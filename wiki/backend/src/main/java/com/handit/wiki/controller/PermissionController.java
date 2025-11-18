package com.handit.wiki.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.handit.wiki.dto.PermissionRequest;
import com.handit.wiki.model.Permission;
import com.handit.wiki.model.PermissionTargetType;
import com.handit.wiki.service.PermissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permissions")
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<Permission>> listPermissions(@PathVariable PermissionTargetType targetType,
            @PathVariable String targetId) {
        return ResponseEntity.ok(permissionService.listPermissions(targetType, targetId));
    }

    @PostMapping
    public ResponseEntity<Permission> savePermission(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.savePermission(request));
    }
}
