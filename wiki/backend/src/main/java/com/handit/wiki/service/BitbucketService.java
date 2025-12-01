package com.handit.wiki.service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
    private final boolean githubProvider;

    @Autowired
    public BitbucketService(RestTemplateBuilder builder, BitbucketProperties properties) {
        this(properties.useMock()
                ? null
                : builder.rootUri(properties.getBaseUrl())
                        .additionalInterceptors((request, body, execution) -> {
                            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getAuthToken());
                            request.getHeaders().add(HttpHeaders.USER_AGENT, "HanditWiki/1.0");
                            if (properties.isGithubProvider()) {
                                request.getHeaders().set(HttpHeaders.ACCEPT, "application/vnd.github+json");
                            }
                            return execution.execute(request, body);
                        })
                        .build(), properties);
    }

    BitbucketService(RestTemplate restTemplate, BitbucketProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.githubProvider = properties.isGithubProvider();
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

        if (githubProvider) {
            return getGithubFileContent(path);
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
                Files.write(file, content);
                LOGGER.info("Mock Bitbucket save: {} ({})", file, commitMessage);
                return "LOCAL-" + Instant.now();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to store file locally", ex);
            }
        }

        if (githubProvider) {
            return saveFileToGithub(path, content, commitMessage);
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

    private String getGithubFileContent(String path) {
        Map<String, Object> fileMetadata = fetchGithubFile(path);
        if (fileMetadata == null || !fileMetadata.containsKey("content")) {
            return "";
        }
        String encodedContent = fileMetadata.get("content").toString();
        byte[] decodedBytes = Base64.getMimeDecoder().decode(encodedContent);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String saveFileToGithub(String path, byte[] content, String commitMessage) {
        Map<String, Object> fileMetadata = fetchGithubFile(path);
        String existingSha = fileMetadata != null && fileMetadata.get("sha") != null
                ? fileMetadata.get("sha").toString()
                : null;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", commitMessage);
        body.put("branch", properties.getBranch());
        body.put("content", Base64.getEncoder().encodeToString(content));
        body.put("committer", Map.of(
                "name", properties.getCommitterName(),
                "email", properties.getCommitterEmail()));
        if (existingSha != null) {
            body.put("sha", existingSha);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                githubContentEndpoint(path),
                HttpMethod.PUT,
                entity,
                Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            return "";
        }

        Object commitNode = responseBody.get("commit");
        if (commitNode instanceof Map<?, ?> commitMap && commitMap.get("sha") != null) {
            return commitMap.get("sha").toString();
        }
        Object contentNode = responseBody.get("content");
        if (contentNode instanceof Map<?, ?> contentMap && contentMap.get("sha") != null) {
            return contentMap.get("sha").toString();
        }
        return "";
    }

    private Map<String, Object> fetchGithubFile(String path) {
        String uri = githubContentEndpointWithRef(path);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
            return response.getBody();
        } catch (NotFound ex) {
            return null;
        }
    }

    private String githubContentEndpoint(String path) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path("/repos/{owner}/{repo}/contents/{path}")
                .buildAndExpand(properties.getWorkspace(), properties.getRepoSlug(), path)
                .toUriString();
    }

    private String githubContentEndpointWithRef(String path) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path("/repos/{owner}/{repo}/contents/{path}")
                .queryParam("ref", properties.getBranch())
                .buildAndExpand(properties.getWorkspace(), properties.getRepoSlug(), path)
                .toUriString();
    }
}
