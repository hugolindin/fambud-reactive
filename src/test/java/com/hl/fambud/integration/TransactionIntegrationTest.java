package com.hl.fambud.integration;

import com.hl.fambud.model.TransactionType;
import com.hl.fambud.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
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
