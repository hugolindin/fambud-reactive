package com.hl.fambud.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.hl.fambud.util.TestDataGenerator.TRANSACTION_BASE_URL;
import static com.hl.fambud.util.TestDataGenerator.TRANSACTION_ID_URL;

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
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(categoryDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    public static TransactionDto postTransaction(WebTestClient webTestClient, Long budgetId, TransactionDto transactionDto) {
        transactionDto.setTransactionId(null);
        return webTestClient
            .post()
            .uri(TRANSACTION_BASE_URL, budgetId)
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transactionDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    public static TransactionDto putTransaction(WebTestClient webTestClient, Long budgetId, TransactionDto transactionDto) {
        return webTestClient
            .put()
            .uri(TRANSACTION_ID_URL, budgetId, transactionDto.getTransactionId())
            .header("Authorization", "Bearer test-token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transactionDto)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    public static TransactionDto getTransaction(WebTestClient webTestClient, Long budgetId, Long transactionId) {
        return webTestClient
            .get()
            .uri(TRANSACTION_ID_URL, budgetId, transactionId)
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    public static void deleteTransaction(WebTestClient webTestClient, Long budgetId, TransactionDto transactionDto) {
        webTestClient.delete()
            .uri(TRANSACTION_ID_URL, budgetId, transactionDto.getTransactionId())
            .header("Authorization", "Bearer test-token")
            .exchange()
            .expectStatus()
            .isNoContent();
    }
}
