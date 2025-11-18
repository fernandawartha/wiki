package com.handit.wiki.dto;

import com.handit.wiki.model.PageStatus;

public record SearchResultDto(
        String id,
        String title,
        String summary,
        String folderId,
        PageStatus status) {
}
