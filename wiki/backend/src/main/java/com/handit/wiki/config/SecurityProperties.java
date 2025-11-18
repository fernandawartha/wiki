package com.handit.wiki.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "security")
@Data
public class SecurityProperties {

    private final Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret = "0123456789ABCDEF0123456789ABCDEF";
        private long expirationHours = 12;
    }

    private java.util.Set<String> superAdmins = new java.util.HashSet<>();
}
