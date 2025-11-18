package com.handit.wiki.dto;

import java.util.Set;

import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionSubjectType;
import com.handit.wiki.model.PermissionTargetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PermissionRequest(
        @NotNull PermissionTargetType targetType,
        @NotBlank String targetId,
        @NotNull PermissionSubjectType subjectType,
        @NotBlank String subjectId,
        @NotEmpty Set<PermissionLevel> levels) {
}
