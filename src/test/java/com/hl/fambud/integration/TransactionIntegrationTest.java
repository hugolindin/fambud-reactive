package com.hl.fambud.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.model.TransactionType;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.util.TestDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class TransactionIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TransactionRepository transactionRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void crud() {
        // create
        TransactionDto createdTransactionDto = post(TestDataGenerator.getTransactionDto());
        assertTransaction(createdTransactionDto);
        // read
        TransactionDto retrievedTransactionDto = get(createdTransactionDto.getTransactionId());
        assertTransaction(retrievedTransactionDto);
        // update
        retrievedTransactionDto.setDescription("Updated Description");
        retrievedTransactionDto.setAmount(BigDecimal.valueOf(100.00));
        TransactionDto updatedTransactionDto = put(retrievedTransactionDto);
        assertEquals("Updated Description", updatedTransactionDto.getDescription());
        assertEquals(0, updatedTransactionDto.getAmount().compareTo(BigDecimal.valueOf(100.00)));

        // delete
        delete(retrievedTransactionDto);
        webTestClient.get()
            .uri(TestDataGenerator.TRANSACTION_BASE_URL + "/{transactionId}", retrievedTransactionDto.getTransactionId())
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    private TransactionDto post(TransactionDto transactionDto) {
        transactionDto.setTransactionId(null);
        return webTestClient
            .post()
            .uri(TestDataGenerator.TRANSACTION_BASE_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transactionDto)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    private TransactionDto get(Long transactionId) {
        return webTestClient
            .get()
            .uri(TestDataGenerator.TRANSACTION_BASE_URL + "/{transactionId}", transactionId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    private TransactionDto put(TransactionDto transactionDto) {
        return webTestClient
            .put()
            .uri(TestDataGenerator.TRANSACTION_BASE_URL + "/{transactionId}", transactionDto.getTransactionId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(transactionDto)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(TransactionDto.class)
            .returnResult()
            .getResponseBody();
    }

    private void delete(TransactionDto transactionDto) {
        webTestClient.delete()
            .uri(TestDataGenerator.TRANSACTION_BASE_URL + "/{transactionId}", transactionDto.getTransactionId())
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    private void assertTransaction(TransactionDto transactionDto) {
        assertNotNull(transactionDto.getTransactionId());
        assertNotNull(transactionDto.getTransactorId());
        assertNotNull(transactionDto.getBudgetId());
        assertEquals("Initial Description", transactionDto.getDescription());
        assertEquals(TransactionType.EXPENSE, transactionDto.getType());
    }

    @Test
    @Disabled
    public void importTransactionsCsv() throws Exception {
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ClassPathResource("standard-format-transactions-10.csv"))
            .contentType(MediaType.APPLICATION_OCTET_STREAM);
        webTestClient.post()
            .uri("/api/transactions/import/999")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus()
            .isEqualTo(202)
            .expectBody(String.class)
            .consumeWith(response -> {
                String importJobId = response.getResponseBody();
                assertNotNull(importJobId);
                System.out.println("Import Job ID: " + importJobId);
            });
        StepVerifier.create(transactionRepository.findByTransactorId(999L).collectList())
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
        transactionRepository.deleteByTransactorId(999L);
    }
}
