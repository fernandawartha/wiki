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

import com.handit.wiki.dto.PageRequest;
import com.handit.wiki.dto.PageResponse;
import com.handit.wiki.dto.StatusChangeRequest;
import com.handit.wiki.model.Page;
import com.handit.wiki.model.PageHistory;
import com.handit.wiki.service.PageService;
import com.handit.wiki.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pages")
@Validated
public class PageController {

    private final PageService pageService;
    private final UserService userService;

    public PageController(PageService pageService, UserService userService) {
        this.pageService = pageService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PageResponse> getPage(@PathVariable("id") String id) {
        return ResponseEntity.ok(pageService.getPage(id, userService.getCurrentUser()));
    }

    @PostMapping
    public ResponseEntity<PageResponse> createPage(@Valid @RequestBody PageRequest request) {
        return ResponseEntity.ok(pageService.createPage(request, userService.getCurrentUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PageResponse> updatePage(@PathVariable("id") String id, @Valid @RequestBody PageRequest request) {
        return ResponseEntity.ok(pageService.updatePage(id, request, userService.getCurrentUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable("id") String id) {
        pageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<PageHistory>> getHistory(@PathVariable("id") String id) {
        return ResponseEntity.ok(pageService.getHistory(id));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Page> publish(@PathVariable("id") String id, @RequestBody(required = false) StatusChangeRequest request) {
        String notes = request != null ? request.notes() : null;
        return ResponseEntity.ok(pageService.publish(id, userService.getCurrentUser(), notes));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Page> archive(@PathVariable("id") String id, @RequestBody(required = false) StatusChangeRequest request) {
        String notes = request != null ? request.notes() : null;
        return ResponseEntity.ok(pageService.archive(id, userService.getCurrentUser(), notes));
    }
}
