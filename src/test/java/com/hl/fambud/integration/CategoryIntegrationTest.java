package com.hl.fambud.integration;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class CategoryIntegrationTest extends BaseIntegrationTest {

    @Test
    public void crud() {
        // create
        BudgetDto budgetDto = TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto());
        long budgetId = budgetDto.getBudgetId();
        CategoryDto createdCategoryDto = TestUtil.postCategory(
            webTestClient, budgetId, TestDataGenerator.getCategoryDto(budgetId));
        assertCategory(budgetId, createdCategoryDto);
        // read
        CategoryDto retrievedCategoryDto = get(budgetId, createdCategoryDto.getCategoryId());
        assertCategory(budgetId, retrievedCategoryDto);
        // update
        retrievedCategoryDto.setName("Updated Insurance");
        CategoryDto updatedCategoryDto = put(budgetId, retrievedCategoryDto);
        assertEquals("Updated Insurance", updatedCategoryDto.getName());
        // delete
        delete(budgetId, retrievedCategoryDto);
        webTestClient.get()
            .uri(TestDataGenerator.CATEGORY_ID_URL, budgetId, retrievedCategoryDto.getCategoryId())
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
