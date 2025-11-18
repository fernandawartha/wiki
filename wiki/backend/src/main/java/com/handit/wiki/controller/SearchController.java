package com.handit.wiki.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.handit.wiki.dto.SearchResultDto;
import com.handit.wiki.service.PageService;
import com.handit.wiki.service.UserService;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final PageService pageService;
    private final UserService userService;

    public SearchController(PageService pageService, UserService userService) {
        this.pageService = pageService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<SearchResultDto>> search(@RequestParam("q") String query) {
        return ResponseEntity.ok(pageService.search(query, userService.getCurrentUser()));
    }
}
