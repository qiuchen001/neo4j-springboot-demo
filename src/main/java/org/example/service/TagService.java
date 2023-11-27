package org.example.service;

import org.example.constants.tag.TagSource;
import org.example.db.neo4j.Tag;
import org.example.db.neo4j.TagPageTagMapper;
import org.example.db.neo4j.TagRelationship;
import org.example.dto.tag.TagPage;
import org.example.dto.tag.TagPageTag;
import org.example.repository.tag.TagRelationshipRepository;
import org.example.repository.tag.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author zhangchenchen
 */
@Service
@Slf4j
public class TagService {

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagRelationshipRepository tagRelationshipRepository;

    @Autowired
    private Session session;


    /**
     * Add a new tag.
     * @param name The name of the tag
     * @param desc The description of the tag
     * @param parentId The id of the parent tag
     * @return The ID of the new tag
     */
    public String addTag(String name, String desc, String parentId) {
        // Check if a tag with the same name already exists under the parent
        if (parentId != null) {
            List<String> childNames = tagRepository.findChildNames(parentId);
            if (childNames.contains(name)) {
                throw new IllegalArgumentException("A tag with the name '" + name + "' already exists under the parent tag with ID " + parentId);
            }
        }

        // Create the new tag
        Tag tag = new Tag();
        tag.setName(name);
        tag.setDesc(desc);
        tag.setCreateTime(System.currentTimeMillis());
        tag.setUpdateTime(System.currentTimeMillis());
        tag.setSource(TagSource.ADMIN.getName());
        tag = tagRepository.save(tag); // Save the tag and get the persisted entity


        // Create a relationship
        if (parentId != null) {
            Optional<Tag> optionalTag = tagRepository.findById(parentId);
            if (optionalTag.isPresent()) {
                Tag parent = optionalTag.get();
                // Only create a new relationship if it doesn't already exist
                if (!tagRelationshipRepository.existsByParentIdAndChildId(parent.getId(), tag.getId())) {
                    TagRelationship relationship = new TagRelationship(parent, tag);
                    tagRelationshipRepository.save(relationship);
                }
            }
        }

        return tag.getId();
    }

    /**
     * Update an existing tag.
     * @param tagId The ID of the tag to update
     * @param name The new name of the tag
     * @param desc The new description of the tag
     * @param parentId The id of the parent tag
     */
    public void updateTag(String tagId, String name, String desc, String parentId) {

        // Get the existing tag
        Optional<Tag> tagOpt = tagRepository.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new IllegalArgumentException("Tag with id " + tagId + " not found");
        }

        // 当标签来源不为admin时，不允许修改
        if (!tagOpt.get().getSource().equals(TagSource.ADMIN.getName())){
            throw new IllegalArgumentException("Tag with id " + tagId + " is forbidden to be modified");
        }

        // If parentId is not null, check if a tag with the same name already exists under the parent
        if (parentId != null) {
            List<String> otherChildNames = tagRepository.findOtherChildNames(parentId, tagId);
            if (otherChildNames.contains(name)) {
                throw new IllegalArgumentException("A tag with the name '" + name + "' already exists under the parent tag with ID " + parentId);
            }
        }

        // Update the tag
        Tag tag = tagOpt.get();
        tag.setName(name);
        tag.setDesc(desc);
        tag.setUpdateTime(System.currentTimeMillis());
        tag = tagRepository.save(tag);

        if (parentId == null) {
            return;
        }

        // Delete all existing parent relationships
        if (tagRelationshipRepository.existsByChild(tag.getId())) {
            tagRelationshipRepository.deleteByChild(tag.getId());
        }

        // Create a new relationship
        Optional<Tag> parentOpt = tagRepository.findById(parentId);
        if (parentOpt.isPresent()) {
            Tag parent = parentOpt.get();
            // Only create a new relationship if it doesn't already exist
            if (!tagRelationshipRepository.existsByParentIdAndChildId(parent.getId(), tag.getId())) {
                TagRelationship relationship = new TagRelationship(parent, tag);
                tagRelationshipRepository.save(relationship);
            }
        }

    }

    /**
     * Delete a tag.
     * @param tagId The ID of the tag to delete
     */
    public void deleteTag(String tagId) {
        // Get the existing tag
        Optional<Tag> tagOpt = tagRepository.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new IllegalArgumentException("Tag with id " + tagId + " not found");
        }

        // 当标签来源不为admin时，不允许删除
        if (!tagOpt.get().getSource().equals(TagSource.ADMIN.getName())){
            throw new IllegalArgumentException("Tag with id " + tagId + " is forbidden to be deleted");
        }

        tagRepository.deleteTagAndItsDescendants(tagId);
    }

    private String buildQuery(Long id, String name, String desc, String parentName, String createTimeRange, String updateTimeRange) {
        StringBuilder query = new StringBuilder("MATCH (n:tag)");

        if (parentName != null && !parentName.isEmpty()) {
            query.append("<-[:IS_PARENT_OF]-(p:tag)");
        }

        query.append(" WHERE ");

        if (id != null) {
            query.append("n.id = ").append(id).append(" AND ");
        }

        if (name != null && !name.isEmpty()) {
            query.append("n.name =~ '.*").append(name).append(".*' AND ");
        }

        if (desc != null && !desc.isEmpty()) {
            query.append("n.desc =~ '.*").append(desc).append(".*' AND ");
        }

        if (parentName != null && !parentName.isEmpty()) {
            query.append("p.name =~ '.*").append(parentName).append(".*' AND ");
        }

        if (createTimeRange != null && !createTimeRange.isEmpty()) {
            String[] createTimeStrs = createTimeRange.split("-");
            Long createTimeStart = Long.parseLong(createTimeStrs[0]);
            Long createTimeEnd = Long.parseLong(createTimeStrs[1]);
            query.append("n.createTime >= ").append(createTimeStart).append(" AND n.createTime <= ").append(createTimeEnd).append(" AND ");
        }

        if (updateTimeRange != null && !updateTimeRange.isEmpty()) {
            String[] updateTimeStrs = updateTimeRange.split("-");
            Long updateTimeStart = Long.parseLong(updateTimeStrs[0]);
            Long updateTimeEnd = Long.parseLong(updateTimeStrs[1]);
            query.append("n.updateTime >= ").append(updateTimeStart).append(" AND n.updateTime <= ").append(updateTimeEnd).append(" AND ");
        }

        // Check if the last characters are " AND " and remove them
        if (query.substring(Math.max(0, query.length() - 5)).equals(" AND ")) {
            query.setLength(query.length() - 5);
        } else {
            // If no conditions were added, remove the trailing " WHERE"
            query.setLength(query.length() - 6);
        }

        return query.toString();
    }

    /**
     * Get a page of tags.
     * @param id The ID of the tag
     * @param name The name of the tag
     * @param desc The description of the tag
     * @param parentName The name of the parent tag
     * @param createTimeRange The create time range of the tag
     * @param updateTimeRange The update time range of the tag
     * @param page The page number
     * @param pagesize The page size
     * @return A page of tags
     */
    public TagPage tagPageList(Long id, String name, String desc, String parentName, String createTimeRange, String updateTimeRange, int page, int pagesize) {
        String baseQuery = buildQuery(id, name, desc, parentName, createTimeRange, updateTimeRange);
        int skip = (page - 1) * pagesize;
        String pageQuery = baseQuery + " OPTIONAL MATCH (n)<-[:IS_PARENT_OF]-(p) RETURN n, p ORDER BY n.id SKIP " + skip + " LIMIT " + pagesize;
        List<TagPageTag> tagList = executeQueryAndReturnTagList(pageQuery);

        String totalQuery = baseQuery + " RETURN count(n)";
        int total = executeCountQuery(totalQuery);

        return new TagPage(tagList, total);
    }

    public List<TagPageTag> executeQueryAndReturnTagList(String query) {
        Result result = session.query(query, Collections.emptyMap());
        List<TagPageTag> tags = new ArrayList<>();
        for (Map<String, Object> row : result) {
            Tag tag = (Tag) row.get("n");
            if (tag == null) {
                System.out.println("Tag is null");
                continue;
            }

            TagPageTag tagPageTag = TagPageTagMapper.convertToDTO(tag);

            if (row.containsKey("p")) {
                Tag parent = (Tag) row.get("p");
                if (parent == null) {
                    System.out.println("Parent is null");
                } else {
                    String parentName = parent.getName();
                    if (parentName == null) {
                        System.out.println("Parent name is null");
                    }
                    tagPageTag.setParentName(parentName); // Assuming Tag class has a field 'parentName' and it's okay to set it to null
                }
            }
            tags.add(tagPageTag);
        }
        return tags;
    }

    public int executeCountQuery(String query) {
        Number count = session.queryForObject(Number.class, query, Collections.emptyMap());
        return (count != null) ? count.intValue() : 0;
    }

    /**
     * Import tags and relationships from a CSV file.
     *
     * @param filePath The path of the CSV file
     * @throws IOException If an I/O error occurs
     */
    public void importTagsAndRelationshipsFromCsv(String filePath) throws IOException {
        Map<String, Tag> tagMap = new HashMap<>(); // For storing tags by name

        // First pass: read the CSV file and save all tags
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // Ignore the first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                String tagName = fields[0];
                Tag tag = tagRepository.findByName(tagName);
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tag.setDesc(fields[1]);
                    tag.setCreateTime(System.currentTimeMillis());
                    tag.setUpdateTime(System.currentTimeMillis());
                    tag = tagRepository.save(tag); // Save the tag and get the persisted entity
                }

                tagMap.put(tag.getName(), tag); // Store the tag in the map
            }
        }

        // Second pass: read the CSV file again and create relationships
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            reader.readLine(); // Ignore the first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                Tag child = tagMap.get(fields[0]); // Get the child tag from the map

                // If this tag has a parent, create a relationship
                if (fields.length >= 4 && !fields[3].isEmpty()) {
                    Tag parent = tagMap.get(fields[3]);
                    if (parent != null) {
                        // Only create a new relationship if it doesn't already exist
                        if (!tagRelationshipRepository.existsByParentIdAndChildId(parent.getId(), child.getId())) {
                            TagRelationship relationship = new TagRelationship(parent, child);
                            tagRelationshipRepository.save(relationship);
                        }
                    }
                }
            }
        }
    }

    /**
     * Import tags from a CSV file.
     * @param filePath The path of the CSV file
     */
    public void importBaiDuTags(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            Tag parent = null;

            reader.readLine(); // Ignore the first line
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                if (fields.length < 2) {
                    continue;
                }

                if (fields[0] != null && !fields[0].isEmpty()){
                    String tagName = fields[0];

                    Tag tag = new Tag();
                    tag.setName(tagName);
                    tag.setCreateTime(System.currentTimeMillis());
                    tag.setUpdateTime(System.currentTimeMillis());
                    tag.setSource(TagSource.NATIVE.getName());
                    tagRepository.save(tag); // Save the tag and get the persisted entity
                    parent = tag;
                }

                if (fields[1] != null && !fields[1].isEmpty()){
                    String tagName = fields[1];

                    Tag tag = new Tag();
                    tag.setName(tagName);
                    tag.setCreateTime(System.currentTimeMillis());
                    tag.setUpdateTime(System.currentTimeMillis());
                    tag.setSource(TagSource.NATIVE.getName());
                    tagRepository.save(tag); // Save the tag and get the persisted entity

                    if (parent != null) {
                        // Only create a new relationship if it doesn't already exist
                        if (!tagRelationshipRepository.existsByParentIdAndChildId(parent.getId(), tag.getId())) {
                            TagRelationship relationship = new TagRelationship(parent, tag);
                            tagRelationshipRepository.save(relationship);
                        }
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Get the tag hierarchy.
     * @return The tag hierarchy
     */
    public List<TagPageTag> getTagHierarchy() {
        List<Tag> roots = findRootTags();
        List<TagPageTag> tagHierarchy = new ArrayList<>();;

        for (Tag tag : roots) {
            TagPageTag tagPageTag = TagPageTagMapper.convertToDTO(tag);
            loadChildren(tagPageTag);
            tagHierarchy.add(tagPageTag);
        }


        return tagHierarchy;
    }

    private List<Tag> findRootTags() {
        return tagRepository.findRoots();
    }

    private void loadChildren(TagPageTag parent) {
        Iterable<TagRelationship> relationships = tagRelationshipRepository.findByParentId(parent.getId());
        List<TagRelationship> sortedRelationships = StreamSupport.stream(relationships.spliterator(), false)
                .sorted(Comparator.comparing(r -> r.getChild().getCreateTime()))
                .collect(Collectors.toList());

        for (TagRelationship relationship : sortedRelationships) {
            Tag childTag = relationship.getChild();
            if (childTag != null) {
                TagPageTag tagPageTag = TagPageTagMapper.convertToDTO(childTag);
                parent.getChildren().add(tagPageTag);
                loadChildren(tagPageTag);
            }
        }
    }

}
