package com.handit.wiki.controller;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.handit.wiki.config.BitbucketProperties;
import com.handit.wiki.service.BitbucketService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final BitbucketService bitbucketService;
    private final BitbucketProperties properties;

    public UploadController(BitbucketService bitbucketService, BitbucketProperties properties) {
        this.bitbucketService = bitbucketService;
        this.properties = properties;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "-" + file.getOriginalFilename();
        String path = "assets/" + filename;
        String commit = bitbucketService.saveBinaryFile(path, file.getBytes(), "chore: upload asset " + filename);
        String url = String.format("https://bitbucket.org/%s/%s/raw/%s/%s",
                properties.getWorkspace(), properties.getRepoSlug(), properties.getBranch(), path);
        return ResponseEntity.ok(Map.of(
                "path", path,
                "url", url,
                "commitHash", commit));
    }
}
