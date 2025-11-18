package com.handit.wiki.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.handit.wiki.config.BitbucketProperties;

class BitbucketServiceTest {

    private BitbucketProperties properties;
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private BitbucketService service;

    @BeforeEach
    void setup() {
        properties = new BitbucketProperties();
        properties.setBaseUrl("https://api.bitbucket.org/2.0");
        properties.setWorkspace("workspace");
        properties.setRepoSlug("repo");
        properties.setBranch("main");
        properties.setAuthToken("token");

        restTemplate = new RestTemplate();
        service = new BitbucketService(restTemplate, properties);
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    void loadsFileContent() {
        server.expect(requestTo("https://api.bitbucket.org/2.0/repositories/workspace/repo/src/main/docs/page.md"))
                .andRespond(withSuccess("# doc", MediaType.TEXT_PLAIN));

        String content = service.getFileContent("docs/page.md");
        assertThat(content).isEqualTo("# doc");
    }

    @Test
    void writesFileAndReturnsCommit() {
        server.expect(requestTo("https://api.bitbucket.org/2.0/repositories/workspace/repo/src"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"hash\":\"abc123\"}", MediaType.APPLICATION_JSON));

        String hash = service.saveFileContent("docs/page.md", "# New content", "update");
        assertThat(hash).isEqualTo("abc123");
    }
}
