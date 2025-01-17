package com.hl.fambud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static com.hl.fambud.util.TestDataGenerator.TEST_BUDGET_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class CategoryIntegrationTest extends BaseIntegrationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        super.init();
        objectMapper = TestUtil.getObjectMapper();
    }

    @Test
    public void crud() {
        // create
        CategoryDto createdCategoryDto = TestUtil.postCategory(
            webTestClient, TEST_BUDGET_ID, TestDataGenerator.getCategoryDto());
        assertCategory(TEST_BUDGET_ID, createdCategoryDto);
        // read
        CategoryDto retrievedCategoryDto = get(TEST_BUDGET_ID, createdCategoryDto.getCategoryId());
        assertCategory(TEST_BUDGET_ID, retrievedCategoryDto);
        // update
        retrievedCategoryDto.setName("Updated Insurance");
        CategoryDto updatedCategoryDto = put(TEST_BUDGET_ID, retrievedCategoryDto);
        assertEquals("Updated Insurance", updatedCategoryDto.getName());
        // delete
        delete(TEST_BUDGET_ID, retrievedCategoryDto);
        webTestClient.get()
            .uri(TestDataGenerator.CATEGORY_ID_URL, TEST_BUDGET_ID, retrievedCategoryDto.getCategoryId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    private CategoryDto get(Long budgetId, Long categoryId) {
        return webTestClient
            .get()
            .uri(TestDataGenerator.CATEGORY_ID_URL , budgetId, categoryId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    private CategoryDto put(Long budgetId, CategoryDto categoryDto) {
        return webTestClient
            .put()
            .uri(TestDataGenerator.CATEGORY_ID_URL, budgetId, categoryDto.getCategoryId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(categoryDto)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    private void delete(Long budgetId, CategoryDto categoryDto) {
        webTestClient.delete()
            .uri(TestDataGenerator.CATEGORY_ID_URL, budgetId, categoryDto.getCategoryId())
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    private void assertCategory(Long budgetId, CategoryDto categoryDto) {
        assertNotNull(categoryDto.getCategoryId());
        assertEquals(budgetId, categoryDto.getBudgetId());
        assertEquals("Insurance", categoryDto.getName());
    }
}
