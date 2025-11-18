package com.handit.wiki.dto;

import com.handit.wiki.model.PageStatus;

import jakarta.validation.constraints.NotBlank;

public record PageRequest(
        @NotBlank String title,
        @NotBlank String folderId,
        String summary,
        @NotBlank String bitbucketPath,
        @NotBlank String content,
        PageStatus status) {
}
