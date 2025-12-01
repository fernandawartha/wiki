package com.handit.wiki.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "bitbucket")
@Data
public class BitbucketProperties {

    public enum Provider {
        BITBUCKET,
        GITHUB
    }

    private String baseUrl = "https://api.bitbucket.org/2.0";
    private Provider provider = Provider.BITBUCKET;
    private String workspace;
    private String repoSlug;
    private String branch = "main";
    private String authToken;
    private String localPath = "./bitbucket-local";
    private String rawBaseUrl;
    private String committerName = "Handit Wiki";
    private String committerEmail = "handit-wiki@local";

    public boolean useMock() {
        return workspace == null || workspace.isBlank()
                || repoSlug == null || repoSlug.isBlank()
                || authToken == null || authToken.isBlank()
                || authToken.equalsIgnoreCase("change-me");
    }

    public boolean isGithubProvider() {
        return provider == Provider.GITHUB;
    }

    public String resolvedRawBaseUrl() {
        if (rawBaseUrl != null && !rawBaseUrl.isBlank()) {
            return rawBaseUrl;
        }
        return isGithubProvider() ? "https://raw.githubusercontent.com" : "https://bitbucket.org";
    }

    public String buildRawFileUrl(String path) {
        if (isGithubProvider()) {
            return String.format("%s/%s/%s/%s/%s",
                    resolvedRawBaseUrl(), workspace, repoSlug, branch, path);
        }
        return String.format("%s/%s/%s/raw/%s/%s",
                resolvedRawBaseUrl(), workspace, repoSlug, branch, path);
    }
}
