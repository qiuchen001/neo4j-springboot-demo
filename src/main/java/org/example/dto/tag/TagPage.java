package org.example.dto.tag;

import java.util.List;

public class TagPage {
    private final List<TagPageTag> tags;
    private final int total;

    public TagPage(List<TagPageTag> tags, int total) {
        this.tags = tags;
        this.total = total;
    }

    public List<TagPageTag> getTags() {
        return tags;
    }

    public int getTotal() {
        return total;
    }

}


