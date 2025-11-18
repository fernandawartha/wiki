package com.handit.wiki.dto;

import java.util.List;

public record TreeNodeDto(
        String id,
        String label,
        String type,
        String parentId,
        List<TreeNodeDto> children) {
}
