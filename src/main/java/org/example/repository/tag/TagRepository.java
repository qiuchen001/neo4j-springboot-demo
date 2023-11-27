package org.example.repository.tag;

import org.example.db.neo4j.Tag;
import feign.Param;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends Neo4jRepository<Tag, String>
{

    /**
     * 创建两个节点，并指定其中一个节点为另一个节点的父节点
     */
    @Query("MERGE (parent:tag {name: $name1}) " +
            "MERGE (child:tag {name: $name2}) " +
            "MERGE (parent)-[:IS_PARENT_OF]->(child)")
    void createRelationship(String name1, String name2);

    /**
     * 查询某个节点的所有子节点
     * @param pId 父节点的id
     * @return 子节点列表
     */
    @Query("MATCH (t:tag)-[:IS_PARENT_OF]->(child) WHERE id(t) = $pId RETURN child")
    List<Tag> findChildList(Long pId);

    /**
     * 查询某个节点的所有子节点
     * @param name 父节点的名称
     * @return 子节点列表
     */
    @Query("MATCH (parent:tag)-[:IS_PARENT_OF]->(child:tag) WHERE parent.name = $name RETURN child")
    List<Tag> findChildList(String name);

    /**
     * 查询当前节点的父节点
     * @param name 当前节点的名称
     * @return 父节点列表
     */
    @Query("MATCH (parent:tag)-[:IS_PARENT_OF]->(child:tag) WHERE child.name = $name RETURN parent")
    List<Tag> findParentList(String name);

    /**
     * 删除所有label为标签的节点
     */
    @Override
    @Query("MATCH (n:tag) DETACH DELETE n")
    void deleteAll();

    /**
     * 根据名称删除节点
     * @param name 节点名称
     */
    @Query("MATCH (n:tag {name: $name}) DETACH DELETE n")
    void deleteByName(String name);

    /**
     * 根据名称查询节点
     * @param name 节点名称
     * @return 节点
     */
    Tag findByName(String name);

    /**
     * 根据标签名称查询根节点
     * @param name 标签名称
     * @return 根节点
     */
    @Query("MATCH (t:tag {name: $name}) WHERE NOT (t)<-[:IS_PARENT_OF]-() RETURN t")
    Tag findRootByName(@Param("name") String name);

    /**
     * 查询所有根节点
     * @return 根节点列表
     */
    @Query("MATCH (t:tag) WHERE NOT (t)<-[:IS_PARENT_OF]-() RETURN t ORDER BY t.createTime ASC")
    List<Tag> findRoots();

    /**
     * 查询子节点名称列表
     * @param pId 父节点的id
     * @return 子节点名称列表
     */
    @Query("MATCH (parent:tag)-[:IS_PARENT_OF]->(child:tag) WHERE id(parent) = $pId RETURN child.name")
    List<String> findChildNames(String pId);

    /**
     *  获取一个标签的所有直接子标签，但是排除一个特定的子标签
     * @param pId 父节点的id
     * @param cId 子节点的id
     * @return 子节点名称列表
     */
    @Query("MATCH (parent:tag)-[:IS_PARENT_OF]->(child:tag) WHERE id(parent) = $pId AND id(child) <> $cId RETURN child.name")
    List<String> findOtherChildNames(String pId, String cId);

    /**
     * 删除节点及其下的所有节点
     * @param tagId 节点id
     */
    @Query("MATCH (n:tag) WHERE n.id = {tagId} OPTIONAL MATCH (n)-[r:IS_PARENT_OF*]->(m:tag) DETACH DELETE n, m")
    void deleteTagAndItsDescendants(String tagId);
}