package org.example.repository.tag;

import org.example.db.neo4j.TagRelationship;
import feign.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRelationshipRepository extends Neo4jRepository<TagRelationship, Long> {

    @Query("MATCH (p:tag)-[r:IS_PARENT_OF]->(c:tag) WHERE p.id = $parentId AND c.id = $childId RETURN COUNT(r) > 0")
    boolean existsByParentIdAndChildId(@Param("parentId") String parentId, @Param("childId") String childId);

    @Query("MATCH (p:tag)-[:IS_PARENT_OF]->(c:tag {id: $childId}) RETURN count(p) > 0")
    boolean existsByChild(String childId);

    @Query("MATCH (p:tag)-[r:IS_PARENT_OF]->(c:tag {id: $childId}) DELETE r")
    void deleteByChild(@Param("childId") String childId);
    Iterable<TagRelationship> findByParentId(String parentId);
}