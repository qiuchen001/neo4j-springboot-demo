package org.example.db.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RelationshipEntity(type = "IS_PARENT_OF")
public class TagRelationship implements Serializable {

    static final String RELATION_TYPE = "IS_PARENT_OF";

    @Id
    @GeneratedValue
    private Long id;

    @StartNode
    private Tag parent;

    @EndNode
    private Tag child;

    @Property
    private String relation;

    public TagRelationship(Tag parent, Tag child) {
        this.parent = parent;
        this.child = child;
        this.relation = RELATION_TYPE;
    }
}