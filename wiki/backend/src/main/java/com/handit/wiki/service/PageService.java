package com.handit.wiki.service;

import java.util.EnumSet;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.handit.wiki.dto.PageRequest;
import com.handit.wiki.dto.PageResponse;
import com.handit.wiki.dto.SearchResultDto;
import com.handit.wiki.model.Page;
import com.handit.wiki.model.PageHistory;
import com.handit.wiki.model.PageStatus;
import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.model.User;
import com.handit.wiki.repository.PageHistoryRepository;
import com.handit.wiki.repository.PageRepository;

@Service
public class PageService {

    private final PageRepository pageRepository;
    private final PageHistoryRepository pageHistoryRepository;
    private final BitbucketService bitbucketService;
    private final PermissionService permissionService;

    public PageService(PageRepository pageRepository,
            PageHistoryRepository pageHistoryRepository,
            BitbucketService bitbucketService,
            PermissionService permissionService) {
        this.pageRepository = pageRepository;
        this.pageHistoryRepository = pageHistoryRepository;
        this.bitbucketService = bitbucketService;
        this.permissionService = permissionService;
    }

    public PageResponse getPage(String id, User user) {
        Page page = findPageOrThrow(id);
        ensureViewPermission(user, page);
        String content = bitbucketService.getFileContent(page.getBitbucketPath());
        return toResponse(page, content);
    }

    public PageResponse createPage(PageRequest request, User user) {
        ensureCreatePermission(user, request.folderId());
        String commitHash = bitbucketService.saveFileContent(request.bitbucketPath(), request.content(),
                "feat: create page " + request.title());
        Page page = Page.builder()
                .title(request.title())
                .folderId(request.folderId())
                .summary(request.summary())
                .bitbucketPath(request.bitbucketPath())
                .status(request.status() != null ? request.status() : PageStatus.DRAFT)
                .createdBy(user != null ? user.getId() : null)
                .updatedBy(user != null ? user.getId() : null)
                .deleted(false)
                .build();
        Page saved = pageRepository.save(page);
        recordHistory(saved, user, commitHash, "Created");
        permissionService.ensureOwnerPermissions(
                user != null ? user.getId() : null, PermissionTargetType.PAGE, saved.getId());
        return toResponse(saved, request.content());
    }

    public PageResponse updatePage(String id, PageRequest request, User user) {
        Page page = findPageOrThrow(id);
        ensureEditPermission(user, page);
        page.setTitle(request.title());
        page.setSummary(request.summary());
        page.setFolderId(request.folderId());
        page.setStatus(request.status() != null ? request.status() : page.getStatus());
        page.setBitbucketPath(request.bitbucketPath());
        page.setUpdatedBy(user != null ? user.getId() : null);

        String commitHash = bitbucketService.saveFileContent(request.bitbucketPath(), request.content(),
                "chore: update page " + request.title());

        Page saved = pageRepository.save(page);
        recordHistory(saved, user, commitHash, "Updated");
        return toResponse(saved, request.content());
    }

    public void deletePage(String id) {
        Page page = findPageOrThrow(id);
        page.setDeleted(true);
        pageRepository.save(page);
    }

    public List<PageHistory> getHistory(String pageId) {
        return pageHistoryRepository.findByPageIdOrderByVersionNumberDesc(pageId);
    }

    public Page publish(String id, User user, String notes) {
        Page page = findPageOrThrow(id);
        ensureEditPermission(user, page);
        page.setStatus(PageStatus.PUBLISHED);
        page.setUpdatedBy(user != null ? user.getId() : null);
        recordHistory(pageRepository.save(page), user, null, notes != null ? notes : "Published");
        return page;
    }

    public Page archive(String id, User user, String notes) {
        Page page = findPageOrThrow(id);
        ensureEditPermission(user, page);
        page.setStatus(PageStatus.ARCHIVED);
        page.setUpdatedBy(user != null ? user.getId() : null);
        recordHistory(pageRepository.save(page), user, null, notes != null ? notes : "Archived");
        return page;
    }

    public List<SearchResultDto> search(String query, User user) {
        return pageRepository.findByTitleContainingIgnoreCaseAndDeletedFalse(query).stream()
                .filter(page -> {
                    if (page.getStatus() == PageStatus.DRAFT) {
                        return permissionService.resolvePagePermissions(user, page.getId())
                                .contains(PermissionLevel.EDIT);
                    }
                    return permissionService.resolvePagePermissions(user, page.getId())
                            .contains(PermissionLevel.VIEW);
                })
                .map(page -> new SearchResultDto(page.getId(), page.getTitle(), page.getSummary(), page.getFolderId(),
                        page.getStatus()))
                .toList();
    }

    private void recordHistory(Page page, User user, String commitHash, String notes) {
        PageHistory history = PageHistory.builder()
                .pageId(page.getId())
                .versionNumber(nextVersionNumber(page.getId()))
                .authorId(user != null ? user.getId() : null)
                .bitbucketCommitHash(commitHash)
                .notes(notes)
                .build();
        pageHistoryRepository.save(history);
    }

    private int nextVersionNumber(String pageId) {
        return pageHistoryRepository.findByPageIdOrderByVersionNumberDesc(pageId).stream()
                .map(PageHistory::getVersionNumber)
                .findFirst()
                .map(v -> v + 1)
                .orElse(1);
    }

    private void ensureViewPermission(User user, Page page) {
        EnumSet<PermissionLevel> permissions = permissionService.resolvePagePermissions(user, page.getId());
        if (page.getStatus() == PageStatus.DRAFT && !permissions.contains(PermissionLevel.EDIT)) {
            throw new SecurityException("Page in draft. Edit permission required.");
        }
        if (page.getStatus() != PageStatus.DRAFT && !permissions.contains(PermissionLevel.VIEW)
                && !permissions.contains(PermissionLevel.EDIT)) {
            throw new SecurityException("Missing view permission");
        }
    }

    private void ensureEditPermission(User user, Page page) {
        EnumSet<PermissionLevel> permissions = permissionService.resolvePagePermissions(user, page.getId());
        if (!permissions.contains(PermissionLevel.EDIT)) {
            throw new SecurityException("Missing edit permission");
        }
    }

    private void ensureCreatePermission(User user, String folderId) {
        if (folderId == null) {
            return;
        }
        boolean allowed = permissionService.hasPermission(user, PermissionTargetType.FOLDER, folderId,
                PermissionLevel.CREATE);
        if (!allowed) {
            throw new SecurityException("Missing create permission");
        }
    }

    private PageResponse toResponse(Page page, String content) {
        return new PageResponse(page.getId(), page.getTitle(), page.getFolderId(), page.getStatus(), page.getSummary(),
                page.getBitbucketPath(), content, page.getCreatedAt(), page.getUpdatedAt(), page.getCreatedBy(),
                page.getUpdatedBy());
    }

    private Page findPageOrThrow(String id) {
        return pageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
    }
}
