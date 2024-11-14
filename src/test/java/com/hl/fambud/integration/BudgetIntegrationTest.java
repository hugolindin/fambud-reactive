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

    private BudgetDto budgetDto;

    private ObjectMapper objectMapper;

    @Autowired
    private DatabaseClient databaseClient;


    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        budgetDto = TestDataGenerator.getBudgetDto();
        budgetRepository.deleteAll().block();
        categoryRepository.deleteAll().block();
        transactorRepository.deleteAll().block();
        transactionRepository.deleteAll().block();
    }

    @Test
    public void crud() throws Exception {
        // create
        BudgetDto createdBudgetDto = create();
        assertBudget(createdBudgetDto);
        Long createdBudgetId = createdBudgetDto.getBudgetId();
        transactorRepository.findAll()
            .doOnNext(transactor -> log.debug("transactor " + transactor));
        // read
        BudgetDto retrievedBudgetDto = webTestClient
            .get()
            .uri(TestDataGenerator.BUDGET_BASE_URL + "/{budgetId}", createdBudgetId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(BudgetDto.class)
            .returnResult()
            .getResponseBody();
        assertBudget(retrievedBudgetDto);
    }

    private BudgetDto create() {
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
}
