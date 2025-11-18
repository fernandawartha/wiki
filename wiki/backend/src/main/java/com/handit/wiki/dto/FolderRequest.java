package com.handit.wiki.dto;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
        @NotBlank String name,
        String description,
        String parentFolderId,
        Integer sortOrder) {
}
