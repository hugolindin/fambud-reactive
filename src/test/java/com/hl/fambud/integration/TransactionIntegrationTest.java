package com.hl.fambud.integration;

import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.model.TransactionType;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.hl.fambud.util.TestDataGenerator.TRANSACTION_CATEGORIES_URL;
import static com.hl.fambud.util.TestDataGenerator.TRANSACTION_ID_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    public void crud() {
        // create
        long budgetId = TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto()).getBudgetId();
        long categoryId = TestUtil.postCategory(
            webTestClient, budgetId, TestDataGenerator.getCategoryDto(budgetId)).getCategoryId();
        TransactionDto createdTransactionDto =
            TestUtil.postTransaction(webTestClient, budgetId, TestDataGenerator.getTransactionDto(budgetId, categoryId));
        assertTransaction(createdTransactionDto);
        // read
        TransactionDto retrievedTransactionDto =
            TestUtil.getTransaction(webTestClient, budgetId, createdTransactionDto.getTransactionId());
        assertTransaction(retrievedTransactionDto);
        // update
        retrievedTransactionDto.setDescription("Updated Description");
        retrievedTransactionDto.setAmount(BigDecimal.valueOf(100.00));
        TransactionDto updatedTransactionDto =
            TestUtil.putTransaction(webTestClient, budgetId, retrievedTransactionDto);
        assertEquals("Updated Description", updatedTransactionDto.getDescription());
        assertEquals(0, updatedTransactionDto.getAmount().compareTo(BigDecimal.valueOf(100.00)));
        // delete
        TestUtil.deleteTransaction(webTestClient, budgetId, retrievedTransactionDto);
        webTestClient.get()
            .uri(TRANSACTION_ID_URL,
                budgetId, retrievedTransactionDto.getTransactionId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    private void assertTransaction(TransactionDto transactionDto) {
        assertNotNull(transactionDto.getTransactionId());
        assertNotNull(transactionDto.getBudgetId());
        assertEquals("Initial Description", transactionDto.getDescription());
        assertEquals(TransactionType.EXPENSE, transactionDto.getType());
    }

    @Test
    @Disabled
    public void importTransactionsCsv() throws Exception {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ClassPathResource("standard-format-transactions-10.csv"))
            .header("Content-Disposition", "form-data; name=file; filename=standard-format-transactions-10.csv")
            .contentType(MediaType.TEXT_PLAIN); // Change to the content type of your CSV file

        webTestClient.post()
            .uri("/api/transactions/import/999")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus()
            .isAccepted()
            .expectBody(String.class)
            .consumeWith(response -> {
                String importJobId = response.getResponseBody();
                assertNotNull(importJobId);
                System.out.println("Import Job ID: " + importJobId);
            });

        StepVerifier.create(transactionRepository.findByBudgetId(999L).collectList())
            .assertNext(transactions -> {
                assertEquals(9, transactions.size());
                assertEquals(7, transactions.stream()
                    .filter(transaction -> transaction.getType() == TransactionType.EXPENSE)
                    .count());
                assertEquals(2, transactions.stream()
                    .filter(transaction -> transaction.getType() == TransactionType.INCOME)
                    .count()
                );
            })
            .verifyComplete();
    }

    @Test
    public void updateWithInvalidBudgetId() {
        long budgetId = TestUtil.postBudget(webTestClient, TestDataGenerator.getBudgetDto()).getBudgetId();
        long categoryId = TestUtil.postCategory(
            webTestClient, budgetId, TestDataGenerator.getCategoryDto(budgetId)).getCategoryId();
        TransactionDto createdTransactionDto = TestUtil.postTransaction(
            webTestClient, budgetId, TestDataGenerator.getTransactionDto(budgetId, categoryId));
        assertTransaction(createdTransactionDto);
        failingPut(-1L, createdTransactionDto);
        failingPut(null, createdTransactionDto);
    }

    private void failingPut(Long invalidBudgetId, TransactionDto transactionDto) {
        webTestClient
            .put()
            .uri(TRANSACTION_CATEGORIES_URL, invalidBudgetId, transactionDto.getTransactionId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transactionDto)
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

}
