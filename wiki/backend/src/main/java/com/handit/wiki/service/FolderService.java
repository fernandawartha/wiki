package com.handit.wiki.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.handit.wiki.dto.FolderRequest;
import com.handit.wiki.dto.TreeNodeDto;
import com.handit.wiki.model.Folder;
import com.handit.wiki.model.Page;
import com.handit.wiki.model.PageStatus;
import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.FolderRepository;
import com.handit.wiki.repository.PageRepository;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final PageRepository pageRepository;
    private final PermissionService permissionService;

    public FolderService(FolderRepository folderRepository, PageRepository pageRepository,
            PermissionService permissionService) {
        this.folderRepository = folderRepository;
        this.pageRepository = pageRepository;
        this.permissionService = permissionService;
    }

    public List<TreeNodeDto> getTree(User user) {
        List<Folder> folders = folderRepository.findByDeletedFalse();
        List<Page> pages = pageRepository.findByDeletedFalse();

        Map<String, List<Folder>> foldersByParent = folders.stream()
                .collect(Collectors.groupingBy(f -> f.getParentFolderId() == null ? "ROOT" : f.getParentFolderId()));
        Map<String, List<Page>> pagesByFolder = pages.stream()
                .collect(Collectors.groupingBy(p -> p.getFolderId() == null ? "ROOT" : p.getFolderId()));

        return buildTree("ROOT", foldersByParent, pagesByFolder, user);
    }

    private List<TreeNodeDto> buildTree(String parentKey, Map<String, List<Folder>> foldersByParent,
            Map<String, List<Page>> pagesByFolder, User user) {
        List<TreeNodeDto> nodes = new ArrayList<>();

        List<Folder> childrenFolders = foldersByParent.getOrDefault(parentKey, List.of());
        childrenFolders.stream()
                .sorted(Comparator.comparing(Folder::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(Folder::getName))
                .filter(folder -> permissionService.hasPermission(user, PermissionTargetType.FOLDER, folder.getId(),
                        PermissionLevel.VIEW))
                .forEach(folder -> {
                    List<TreeNodeDto> childNodes = buildTree(folder.getId(), foldersByParent, pagesByFolder, user);
                    nodes.add(new TreeNodeDto(folder.getId(), folder.getName(), "FOLDER", folder.getParentFolderId(),
                            childNodes));
                });

        List<Page> folderPages = pagesByFolder.getOrDefault(parentKey, List.of());
        folderPages.stream()
                .filter(page -> canDisplayPageInTree(user, page))
                .sorted(Comparator.comparing(Page::getTitle))
                .forEach(page -> nodes.add(
                        new TreeNodeDto(page.getId(), page.getTitle(), "PAGE", page.getFolderId(), List.of())));

        return nodes;
    }

    private boolean canDisplayPageInTree(User user, Page page) {
        EnumSet<PermissionLevel> resolved = permissionService.resolvePagePermissions(user, page.getId());
        if (page.getStatus() == PageStatus.DRAFT) {
            return resolved.contains(PermissionLevel.EDIT);
        }
        return resolved.contains(PermissionLevel.VIEW) || resolved.contains(PermissionLevel.EDIT);
    }

    public Folder createFolder(FolderRequest request, User user) {
        Folder folder = Folder.builder()
                .name(request.name())
                .description(request.description())
                .parentFolderId(request.parentFolderId())
                .sortOrder(request.sortOrder())
                .createdBy(user != null ? user.getId() : null)
                .updatedBy(user != null ? user.getId() : null)
                .deleted(false)
                .build();
        Folder saved = folderRepository.save(folder);
        permissionService.ensureOwnerPermissions(
                user != null ? user.getId() : null, PermissionTargetType.FOLDER, saved.getId());
        return saved;
    }

    public Folder updateFolder(String id, FolderRequest request, User user) {
        Folder folder = folderRepository.findById(id).orElseThrow();
        folder.setName(request.name());
        folder.setDescription(request.description());
        folder.setParentFolderId(request.parentFolderId());
        folder.setSortOrder(request.sortOrder());
        folder.setUpdatedBy(user != null ? user.getId() : folder.getUpdatedBy());
        return folderRepository.save(folder);
    }

    public void deleteFolder(String id) {
        Folder folder = folderRepository.findById(id).orElseThrow();
        folder.setDeleted(true);
        folderRepository.save(folder);
    }

    public Folder getFolder(String id) {
        return folderRepository.findById(id).orElse(null);
    }
}
