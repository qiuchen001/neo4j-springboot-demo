package org.example.db.neo4j;

import org.example.dto.tag.TagPageTag;
import java.util.ArrayList;

public class TagPageTagMapper {
    public static TagPageTag convertToDTO(Tag tag) {
        TagPageTag dto = new TagPageTag();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setDesc(tag.getDesc());
        dto.setOperator(tag.getOperator());
        dto.setCreateTime(tag.getCreateTime());
        dto.setUpdateTime(tag.getUpdateTime());
        dto.setSource(tag.getSource());
        dto.setChildren(new ArrayList<>());
        return dto;
    }
}
