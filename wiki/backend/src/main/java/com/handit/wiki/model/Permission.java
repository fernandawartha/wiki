package com.handit.wiki.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    private String id;
    private PermissionTargetType targetType;
    private String targetId;
    private PermissionSubjectType subjectType;
    private String subjectId;
    private Set<PermissionLevel> levels;
}
