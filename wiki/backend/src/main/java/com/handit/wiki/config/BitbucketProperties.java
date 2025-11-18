package com.handit.wiki.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "bitbucket")
@Data
public class BitbucketProperties {
    private String baseUrl = "https://api.bitbucket.org/2.0";
    private String workspace;
    private String repoSlug;
    private String branch = "main";
    private String authToken;
    private String localPath = "./bitbucket-local";

    public boolean useMock() {
        return workspace == null || workspace.isBlank()
                || repoSlug == null || repoSlug.isBlank()
                || authToken == null || authToken.isBlank()
                || authToken.equalsIgnoreCase("change-me");
    }
}
