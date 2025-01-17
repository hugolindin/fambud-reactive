package com.hl.fambud.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

public class TestUtil {

    public static BudgetDto postBudget(WebTestClient webTestClient, BudgetDto budgetDto) {
        TestDataGenerator.setIdsToNull(budgetDto);
        return webTestClient
            .post()
            .uri(TestDataGenerator.BUDGET_BASE_URL)
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(budgetDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(BudgetDto.class)
            .returnResult()
            .getResponseBody();
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    public static CategoryDto postCategory(WebTestClient webTestClient, Long budgetId, CategoryDto categoryDto) {
        categoryDto.setCategoryId(null);
        return webTestClient
            .post()
            .uri(TestDataGenerator.CATEGORY_BASE_URL, budgetId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(categoryDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }
}
