package org.example.dto.tag;

import java.util.List;

public class TagPageTag {
    private String id;
    private String name;
    private String desc;
    private String operator;
    private Long createTime;
    private Long updateTime;
    private String source;

    private String parentName;
    private List<TagPageTag> children;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc=desc;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator=operator;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime=createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime=updateTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source=source;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName=parentName;
    }

    public List<TagPageTag>  getChildren() {
        return children;
    }

    public void setChildren(List<TagPageTag> children) {
        this.children=children;
    }
}



