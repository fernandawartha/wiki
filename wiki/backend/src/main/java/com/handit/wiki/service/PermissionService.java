package com.handit.wiki.service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.handit.wiki.config.SecurityProperties;
import com.handit.wiki.dto.PermissionRequest;
import com.handit.wiki.model.Folder;
import com.handit.wiki.model.Page;
import com.handit.wiki.model.Permission;
import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionSubjectType;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.FolderRepository;
import com.handit.wiki.repository.PageRepository;
import com.handit.wiki.repository.PermissionRepository;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final FolderRepository folderRepository;
    private final PageRepository pageRepository;
    private final SecurityProperties securityProperties;

    public PermissionService(PermissionRepository permissionRepository,
            FolderRepository folderRepository,
            PageRepository pageRepository,
            SecurityProperties securityProperties) {
        this.permissionRepository = permissionRepository;
        this.folderRepository = folderRepository;
        this.pageRepository = pageRepository;
        this.securityProperties = securityProperties;
    }

    public boolean hasPermission(User user, PermissionTargetType targetType, String targetId, PermissionLevel level) {
        if (isSuperAdmin(user)) {
            return true;
        }
        EnumSet<PermissionLevel> permissions = switch (targetType) {
            case PAGE -> resolvePagePermissions(user, targetId);
            case FOLDER -> resolveFolderPermissions(user, targetId);
        };
        return permissions.contains(level);
    }

    public EnumSet<PermissionLevel> resolvePagePermissions(User user, String pageId) {
        if (isSuperAdmin(user)) {
            return EnumSet.allOf(PermissionLevel.class);
        }
        Page page = pageRepository.findById(pageId).orElse(null);
        if (page == null) {
            return EnumSet.noneOf(PermissionLevel.class);
        }
        if (user != null && user.getId() != null && user.getId().equals(page.getCreatedBy())) {
            return EnumSet.allOf(PermissionLevel.class);
        }
        EnumSet<PermissionLevel> direct = resolve(user, PermissionTargetType.PAGE, page.getId());
        if (!direct.isEmpty()) {
            return direct;
        }
        return resolveFolderPermissions(user, page.getFolderId());
    }

    public EnumSet<PermissionLevel> resolveFolderPermissions(User user, String folderId) {
        if (isSuperAdmin(user)) {
            return EnumSet.allOf(PermissionLevel.class);
        }
        if (folderId == null) {
            return EnumSet.noneOf(PermissionLevel.class);
        }
        Folder folder = folderRepository.findById(folderId).orElse(null);
        if (folder == null) {
            return EnumSet.noneOf(PermissionLevel.class);
        }
        if (user != null && user.getId() != null && user.getId().equals(folder.getCreatedBy())) {
            return EnumSet.allOf(PermissionLevel.class);
        }
        EnumSet<PermissionLevel> direct = resolve(user, PermissionTargetType.FOLDER, folder.getId());
        if (!direct.isEmpty()) {
            return direct;
        }
        return resolveFolderPermissions(user, folder.getParentFolderId());
    }

    public List<Permission> listPermissions(PermissionTargetType targetType, String targetId) {
        return permissionRepository.findByTargetTypeAndTargetId(targetType, targetId);
    }

    public Permission savePermission(PermissionRequest request) {
        Permission permission = permissionRepository
                .findByTargetTypeAndTargetIdAndSubjectTypeAndSubjectId(
                        request.targetType(), request.targetId(), request.subjectType(), request.subjectId())
                .orElseGet(Permission::new);
        permission.setTargetType(request.targetType());
        permission.setTargetId(request.targetId());
        permission.setSubjectType(request.subjectType());
        permission.setSubjectId(request.subjectId());
        permission.setLevels(request.levels());
        return permissionRepository.save(permission);
    }

    public void ensureOwnerPermissions(String userId, PermissionTargetType targetType, String targetId) {
        if (userId == null) {
            return;
        }
        if (securityProperties.getSuperAdmins().contains(userId)) {
            // already has implicit permissions
            return;
        }
        permissionRepository
                .findByTargetTypeAndTargetIdAndSubjectTypeAndSubjectId(
                        targetType, targetId, PermissionSubjectType.USER, userId)
                .ifPresentOrElse(permission -> {
                    permission.setLevels(EnumSet.allOf(PermissionLevel.class));
                    permissionRepository.save(permission);
                }, () -> {
                    Permission permission = Permission.builder()
                            .targetType(targetType)
                            .targetId(targetId)
                            .subjectType(PermissionSubjectType.USER)
                            .subjectId(userId)
                            .levels(EnumSet.allOf(PermissionLevel.class))
                            .build();
                    permissionRepository.save(permission);
                });
    }

    private EnumSet<PermissionLevel> resolve(User user, PermissionTargetType targetType, String targetId) {
        if (user == null) {
            return EnumSet.noneOf(PermissionLevel.class);
        }
        List<Permission> permissions = permissionRepository.findByTargetTypeAndTargetId(targetType, targetId);
        EnumSet<PermissionLevel> result = EnumSet.noneOf(PermissionLevel.class);
        for (Permission permission : permissions) {
            if (matches(permission, user)) {
                result.addAll(permission.getLevels());
            }
        }
        return result;
    }

    private boolean matches(Permission permission, User user) {
        if (permission.getSubjectType() == PermissionSubjectType.USER) {
            return permission.getSubjectId().equals(user.getId());
        }
        if (permission.getSubjectType() == PermissionSubjectType.GROUP) {
            Set<String> groups = CollectionUtils.isEmpty(user.getGroupIds()) ? Set.of()
                    : Set.copyOf(user.getGroupIds());
            return groups.contains(permission.getSubjectId());
        }
        return false;
    }

    private boolean isSuperAdmin(User user) {
        if (user == null) {
            return false;
        }
        return securityProperties.getSuperAdmins().stream()
                .anyMatch(value -> value.equalsIgnoreCase(user.getId())
                        || value.equalsIgnoreCase(user.getUsername())
                        || value.equalsIgnoreCase(user.getEmail()));
    }
}
