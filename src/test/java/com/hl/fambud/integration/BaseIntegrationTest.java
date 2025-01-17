package com.hl.fambud.integration;


import com.hl.fambud.configuration.SecurityTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@Import(SecurityTestConfig.class)
public class BaseIntegrationTest {

    @Autowired
    protected WebTestClient webTestClient;

    public void init() {
        webTestClient = webTestClient.mutate()
            .defaultHeader("Authorization", "Bearer test-token")
            .build();
    }
}
