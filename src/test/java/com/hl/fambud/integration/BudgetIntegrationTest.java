package com.hl.fambud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.repository.BudgetRepository;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.repository.TransactorRepository;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class BudgetIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    TransactorRepository transactorRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DatabaseClient databaseClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper = TestUtil.getObjectMapper();
    }

    @Test
    public void bareBudgetCrud() throws Exception {
        BudgetDto createdBudgetDto = TestUtil.postBudget(webTestClient, BudgetDto.builder().name("Budget").build());
        assertNotNull(createdBudgetDto);
        assertNotNull(createdBudgetDto.getBudgetId());
    }

    @Test
    public void fullBudgetCrud() throws Exception {
        BudgetDto createdBudgetDto = TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto());
        assertBudget(createdBudgetDto);
        // read
        BudgetDto retrievedBudgetDto = get(createdBudgetDto.getBudgetId());
        assertBudget(retrievedBudgetDto);
        // update
        updateData(retrievedBudgetDto);
        BudgetDto updatedBudgetDto = put(retrievedBudgetDto);
        assertUpdatedBudget(updatedBudgetDto);
        // delete
        delete(retrievedBudgetDto);
        webTestClient.get()
            .uri(TestDataGenerator.BUDGET_ID_URL, retrievedBudgetDto.getBudgetId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    public void getAll() throws Exception {
        TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto());
        TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto());
        TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto());
        List<BudgetDto> allBudgetDtos = webTestClient
            .get()
            .uri(TestDataGenerator.BUDGET_BASE_URL)
            .exchange()
            .expectBodyList(BudgetDto.class)
            .returnResult()
            .getResponseBody();
        assertNotNull(allBudgetDtos);
        assertEquals(3, allBudgetDtos.size());
        allBudgetDtos.forEach(this::assertBudget);
    }

    private BudgetDto get(Long budgetId) {
        return webTestClient
            .get()
            .uri(TestDataGenerator.BUDGET_ID_URL, budgetId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(BudgetDto.class)
            .returnResult()
            .getResponseBody();
    }

    private BudgetDto put(BudgetDto budgetDto) {
        return webTestClient
            .put()
            .uri(TestDataGenerator.BUDGET_ID_URL, budgetDto.getBudgetId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(budgetDto)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(BudgetDto.class)
            .returnResult()
            .getResponseBody();
    }

    private void delete(BudgetDto budgetDto) {
        webTestClient.delete()
            .uri(TestDataGenerator.BUDGET_ID_URL, budgetDto.getBudgetId())
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    private void updateData(BudgetDto budgetDto) {
        budgetDto.setName("Updated budget name");
        budgetDto.getCategories().forEach(categoryDto ->
            categoryDto.setName("Updated category name"));
        budgetDto.getTransactors().forEach(transactorDto -> {
            transactorDto.setFirstName("Updated first name");
            transactorDto.setLastName("Updated last name");
        });
        budgetDto.getTransactions().forEach(transactionDto -> {
                transactionDto.setDescription("Updated transaction description");
                transactionDto.setAmount(BigDecimal.valueOf(999.99));
        });
    }

    @SneakyThrows
    private void assertBudget(BudgetDto budgetDto) {
        log.debug("assertBudget" +  System.lineSeparator() + objectMapper.writeValueAsString(budgetDto));
        assertNotNull(budgetDto);
        assertNotNull(budgetDto.getBudgetId());
        budgetDto.getCategories().forEach(categoryDto -> {
            assertNotNull(categoryDto.getCategoryId());
            assertEquals(budgetDto.getBudgetId(), categoryDto.getBudgetId());
        });
        assertEquals(2, budgetDto.getTransactors().size());
        budgetDto.getTransactors().forEach(transactorDto -> {
            assertNotNull(transactorDto.getTransactorId());
            assertEquals(budgetDto.getBudgetId(), transactorDto.getBudgetId());
        });
        assertEquals(4, budgetDto.getTransactions().size());
        budgetDto.getTransactions()
            .forEach(transactionDto -> {
                assertNotNull(transactionDto.getTransactionId());
                assertEquals(budgetDto.getBudgetId(), transactionDto.getBudgetId());
        });
    }

    private void assertUpdatedBudget(BudgetDto budgetDto) {
        assertBudget(budgetDto);
        assertEquals("Updated budget name", budgetDto.getName());
        budgetDto.getCategories().forEach(categoryDto ->
            assertEquals("Updated category name", categoryDto.getName()));
        budgetDto.getTransactors().forEach(transactorDto -> {
            assertEquals("Updated first name", transactorDto.getFirstName());
            assertEquals("Updated last name", transactorDto.getLastName());
        });
        budgetDto.getTransactions().forEach(transactionDto -> {
            assertEquals("Updated transaction description", transactionDto.getDescription());
            assertEquals(BigDecimal.valueOf(999.99), transactionDto.getAmount());
        });
    }
}
