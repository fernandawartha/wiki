package com.handit.wiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.handit.wiki.config.BitbucketProperties;
import com.handit.wiki.config.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties({BitbucketProperties.class, SecurityProperties.class})
public class HanditWikiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanditWikiApplication.class, args);
    }
}
