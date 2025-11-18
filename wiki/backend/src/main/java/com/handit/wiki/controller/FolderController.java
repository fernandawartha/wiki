package com.handit.wiki.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.handit.wiki.dto.FolderRequest;
import com.handit.wiki.dto.TreeNodeDto;
import com.handit.wiki.model.Folder;
import com.handit.wiki.model.PermissionLevel;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.service.FolderService;
import com.handit.wiki.service.PermissionService;
import com.handit.wiki.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/folders")
@Validated
public class FolderController {

    private final FolderService folderService;
    private final UserService userService;
    private final PermissionService permissionService;

    public FolderController(FolderService folderService, UserService userService, PermissionService permissionService) {
        this.folderService = folderService;
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @GetMapping("/tree")
    public ResponseEntity<List<TreeNodeDto>> getTree() {
        return ResponseEntity.ok(folderService.getTree(userService.getCurrentUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Folder> getFolder(@PathVariable String id) {
        return ResponseEntity.ofNullable(folderService.getFolder(id));
    }

    @PostMapping
    public ResponseEntity<Folder> createFolder(@Valid @RequestBody FolderRequest request) {
        var user = userService.getCurrentUser();
        if (request.parentFolderId() != null && !permissionService.hasPermission(user, PermissionTargetType.FOLDER,
                request.parentFolderId(), PermissionLevel.CREATE)) {
            throw new SecurityException("Missing permission to create folder");
        }
        return ResponseEntity.ok(folderService.createFolder(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Folder> updateFolder(@PathVariable String id, @Valid @RequestBody FolderRequest request) {
        return ResponseEntity.ok(folderService.updateFolder(id, request, userService.getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
        folderService.deleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}
