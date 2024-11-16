package com.hl.fambud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.repository.BudgetRepository;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.repository.TransactorRepository;
import com.hl.fambud.util.TestDataGenerator;
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

    private BudgetDto sharedBudgetDto;

    private ObjectMapper objectMapper;


    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        sharedBudgetDto = TestDataGenerator.getBudgetDto();
    }

    @Test
    public void crud() throws Exception {
        // create
        BudgetDto createdBudgetDto = post(sharedBudgetDto);
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
            .uri(TestDataGenerator.BUDGET_BASE_URL + "/{budgetId}", retrievedBudgetDto.getBudgetId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    public void getAll() throws Exception {
        post(TestDataGenerator.getBudgetDto());
        post(TestDataGenerator.getBudgetDto());
        post(TestDataGenerator.getBudgetDto());
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

    private BudgetDto post(BudgetDto budgetDto) {
        TestDataGenerator.setIdsToNull(budgetDto);
        return webTestClient
            .post()
            .uri(TestDataGenerator.BUDGET_BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(budgetDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(BudgetDto.class)
            .returnResult()
            .getResponseBody();
    }

    private BudgetDto get(Long budgetId) {
        return webTestClient
            .get()
            .uri(TestDataGenerator.BUDGET_BASE_URL + "/{budgetId}", budgetId)
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
            .uri(TestDataGenerator.BUDGET_BASE_URL + "/{budgetId}", budgetDto.getBudgetId())
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
            .uri(TestDataGenerator.BUDGET_BASE_URL + "/{budgetId}", budgetDto.getBudgetId())
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
            transactorDto.getTransactions().forEach(transactionDto -> {
                transactionDto.setDescription("Updated transaction description");
                transactionDto.setAmount(BigDecimal.valueOf(999.99));
            });
        });
    }

    @SneakyThrows
    private void assertBudget(BudgetDto budgetDto) {
        log.debug("assertBudget \n" + objectMapper.writeValueAsString(budgetDto));
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
        budgetDto.getTransactors().forEach(transactorDto -> {
            assertEquals(2, transactorDto.getTransactions().size());
            transactorDto.getTransactions().forEach(transactionDto -> {
                assertNotNull(transactionDto.getTransactionId());
                assertEquals(transactorDto.getTransactorId(), transactionDto.getTransactorId());
            });
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
            transactorDto.getTransactions().forEach(transactionDto -> {
                assertEquals("Updated transaction description", transactionDto.getDescription());
                assertEquals(BigDecimal.valueOf(999.99), transactionDto.getAmount());
            });
        });
    }
}
