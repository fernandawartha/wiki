package com.handit.wiki.dto;

import java.time.Instant;

import com.handit.wiki.model.PageStatus;

public record PageResponse(
        String id,
        String title,
        String folderId,
        PageStatus status,
        String summary,
        String bitbucketPath,
        String content,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy) {
}
