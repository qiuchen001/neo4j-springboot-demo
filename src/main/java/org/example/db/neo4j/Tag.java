package org.example.db.neo4j;

import lombok.Data;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import java.io.Serializable;

@Data
//@Builder
@NodeEntity(label = "tag")
public class Tag implements Serializable {
    @Id
    @GeneratedValue(strategy = Neo4JCustomIdStrategy.class)
    private String id;

    @Property("name")
    private String name;

    @Property("desc")
    private String desc;

    /**
     * The operator who created this tag
     */
    @Property("operator")
    private String operator = "51admin";

    @Property("createTime")
    private Long createTime;

    @Property("updateTime")
    private Long updateTime;

    @Property("source")
    private String source;
}