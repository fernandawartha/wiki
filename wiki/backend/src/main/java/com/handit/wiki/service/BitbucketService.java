package com.handit.wiki.service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.handit.wiki.config.BitbucketProperties;

@Service
public class BitbucketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BitbucketService.class);

    private final RestTemplate restTemplate;
    private final BitbucketProperties properties;
    private final Path localRepoPath;

    @Autowired
    public BitbucketService(RestTemplateBuilder builder, BitbucketProperties properties) {
        this(properties.useMock()
                ? null
                : builder.rootUri(properties.getBaseUrl())
                        .additionalInterceptors((request, body, execution) -> {
                            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAuthToken());
                            return execution.execute(request, body);
                        })
                        .build(), properties);
    }

    BitbucketService(RestTemplate restTemplate, BitbucketProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.localRepoPath = Path.of(properties.getLocalPath()).toAbsolutePath();
        if (properties.useMock()) {
            try {
                Files.createDirectories(localRepoPath);
                LOGGER.warn("Bitbucket credentials not configured. Using local folder {}", localRepoPath);
            } catch (IOException ex) {
                throw new IllegalStateException("Failed to initialize local Bitbucket mock folder", ex);
            }
        }
    }

    public String getFileContent(String path) {
        if (properties.useMock()) {
            Path file = localRepoPath.resolve(path);
            try {
                if (Files.exists(file)) {
                    byte[] bytes = Files.readAllBytes(file);
                    return new String(bytes, Charset.forName("UTF-8"));
                }
                return "";
            } catch (IOException ex) {
                LOGGER.error("Failed to read mock file {}", file, ex);
                return "";
            }
        }

        String uri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path("/repositories/{workspace}/{repo}/src/{branch}/{path}")
                .buildAndExpand(properties.getWorkspace(), properties.getRepoSlug(), properties.getBranch(), path)
                .toUriString();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return response.getBody();
        } catch (NotFound ex) {
            return "";
        }
    }

    public String saveFileContent(String path, String content, String commitMessage) {
        return saveFile(path, content.getBytes(StandardCharsets.UTF_8), commitMessage);
    }

    public String saveBinaryFile(String path, byte[] content, String commitMessage) {
        return saveFile(path, content, commitMessage);
    }

    private String saveFile(String path, byte[] content, String commitMessage) {
        if (properties.useMock()) {
            Path file = localRepoPath.resolve(path);
            try {
                Files.createDirectories(file.getParent());
                Files.write(file, new String(content, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8));
                LOGGER.info("Mock Bitbucket save: {} ({})", file, commitMessage);
                return "LOCAL-" + Instant.now();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to store file locally", ex);
            }
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("message", commitMessage);
        body.add("branch", properties.getBranch());
        body.add(path, content);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        String uri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path("/repositories/{workspace}/{repo}/src")
                .buildAndExpand(properties.getWorkspace(), properties.getRepoSlug())
                .toUriString();
        ResponseEntity<Map> response = restTemplate.postForEntity(uri, requestEntity, Map.class);
        Map<String, Object> responseBody = response.getBody();
        return responseBody != null && responseBody.containsKey("hash") ? responseBody.get("hash").toString() : "";
    }
}
