package com.hl.fambud.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.configuration.SecurityTestConfig;
import com.hl.fambud.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@Import(SecurityTestConfig.class)
public class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    protected ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        webTestClient = webTestClient.mutate()
            .defaultHeader("Authorization", "Bearer test-token")
            .build();
        objectMapper = TestUtil.getObjectMapper();
    }
}
