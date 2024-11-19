package com.hl.fambud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.util.TestDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class CategoryIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private CategoryDto sharedCategoryDto;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        sharedCategoryDto = TestDataGenerator.getCategoryDto();
    }

    @Test
    public void crud() {
        // create
        CategoryDto createdCategoryDto = post(sharedCategoryDto);
        assertCategory(createdCategoryDto);
        // read
        CategoryDto retrievedCategoryDto = get(createdCategoryDto.getCategoryId());
        assertCategory(retrievedCategoryDto);
        // update
        retrievedCategoryDto.setName("Updated Insurance");
        CategoryDto updatedCategoryDto = put(retrievedCategoryDto);
        assertEquals("Updated Insurance", updatedCategoryDto.getName());
        // delete
        delete(retrievedCategoryDto);
        webTestClient.get()
            .uri(TestDataGenerator.CATEGORY_BASE_URL + "/{categoryId}", retrievedCategoryDto.getCategoryId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    private CategoryDto post(CategoryDto categoryDto) {
        categoryDto.setCategoryId(null);
        return webTestClient
            .post()
            .uri(TestDataGenerator.CATEGORY_BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(categoryDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    private CategoryDto get(Long categoryId) {
        return webTestClient
            .get()
            .uri(TestDataGenerator.CATEGORY_BASE_URL + "/{categoryId}", categoryId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    private CategoryDto put(CategoryDto categoryDto) {
        return webTestClient
            .put()
            .uri(TestDataGenerator.CATEGORY_BASE_URL + "/{categoryId}", categoryDto.getCategoryId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(categoryDto)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(CategoryDto.class)
            .returnResult()
            .getResponseBody();
    }

    private void delete(CategoryDto categoryDto) {
        webTestClient.delete()
            .uri(TestDataGenerator.CATEGORY_BASE_URL + "/{categoryId}", categoryDto.getBudgetId())
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    private void assertCategory(CategoryDto categoryDto) {
        assertNotNull(categoryDto.getCategoryId());
        assertEquals(1L, categoryDto.getCategoryId());
        assertEquals("Insurance", categoryDto.getName());
    }

    private void updateData(CategoryDto categoryDto) {
        categoryDto.setName("Updated Insurance");
    }
}
