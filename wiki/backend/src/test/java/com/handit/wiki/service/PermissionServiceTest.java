package com.handit.wiki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.handit.wiki.config.SecurityProperties;
import com.handit.wiki.model.Folder;
import com.handit.wiki.model.Page;
import com.handit.wiki.model.PageStatus;
import com.handit.wiki.model.Permission;
import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionSubjectType;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.FolderRepository;
import com.handit.wiki.repository.PageRepository;
import com.handit.wiki.repository.PermissionRepository;

class PermissionServiceTest {

    private final PermissionRepository permissionRepository = mock(PermissionRepository.class);
    private final FolderRepository folderRepository = mock(FolderRepository.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final SecurityProperties securityProperties = new SecurityProperties();

    private final PermissionService permissionService = new PermissionService(permissionRepository, folderRepository,
            pageRepository, securityProperties);

    @Test
    void inheritsFromFolderWhenPageLacksExplicitPermission() {
        User user = User.builder().id("user-1").username("fernanda").groupIds(List.of("group-1")).build();
        Page page = Page.builder().id("page-1").folderId("folder-1").status(PageStatus.PUBLISHED).build();
        Folder folder = Folder.builder().id("folder-1").parentFolderId(null).build();

        when(pageRepository.findById("page-1")).thenReturn(java.util.Optional.of(page));
        when(folderRepository.findById("folder-1")).thenReturn(java.util.Optional.of(folder));
        when(permissionRepository.findByTargetTypeAndTargetId(PermissionTargetType.PAGE, "page-1"))
                .thenReturn(List.of());
        when(permissionRepository.findByTargetTypeAndTargetId(PermissionTargetType.FOLDER, "folder-1")).thenReturn(
                List.of(Permission.builder()
                        .targetType(PermissionTargetType.FOLDER)
                        .targetId("folder-1")
                        .subjectType(PermissionSubjectType.USER)
                        .subjectId("user-1")
                        .levels(EnumSet.of(PermissionLevel.VIEW))
                        .build()));

        EnumSet<PermissionLevel> permissions = permissionService.resolvePagePermissions(user, "page-1");
        assertThat(permissions).containsExactly(PermissionLevel.VIEW);
    }

    @Test
    void matchesGroupPermission() {
        User user = User.builder().id("user-1").groupIds(List.of("engineers")).build();
        Folder folder = Folder.builder().id("folder").parentFolderId(null).build();

        when(folderRepository.findById("folder")).thenReturn(java.util.Optional.of(folder));
        when(permissionRepository.findByTargetTypeAndTargetId(PermissionTargetType.FOLDER, "folder")).thenReturn(
                List.of(Permission.builder()
                        .targetType(PermissionTargetType.FOLDER)
                        .targetId("folder")
                        .subjectType(PermissionSubjectType.GROUP)
                        .subjectId("engineers")
                        .levels(EnumSet.of(PermissionLevel.CREATE, PermissionLevel.EDIT))
                        .build()));

        EnumSet<PermissionLevel> permissions = permissionService.resolveFolderPermissions(user, "folder");
        assertThat(permissions).contains(PermissionLevel.CREATE, PermissionLevel.EDIT);
    }
}
