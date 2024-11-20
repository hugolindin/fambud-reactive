package com.hl.fambud.controller;

import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.exception.InvalidPathVariableException;
import com.hl.fambud.service.TransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.hl.fambud.util.BudgetUtil.INVALID_TRANSACTION_ID;

@RestController
@AllArgsConstructor
@RequestMapping("${app.base-url}/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public Mono<ResponseEntity<TransactionDto>> createTransaction(@Valid @RequestBody TransactionDto transactionDto) {
        return transactionService.createTransaction(transactionDto)
            .map(transaction -> new ResponseEntity<>(transaction, HttpStatus.CREATED));
    }

    // Get a transaction by ID
    @GetMapping("/{transactionId}")
    public Mono<ResponseEntity<TransactionDto>> getTransaction(@PathVariable Long transactionId) {
        return transactionService.getTransaction(transactionId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Get all transactions
    @GetMapping
    public Flux<TransactionDto> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // Update an existing transaction
    @PutMapping("/{transactionId}")
    public Mono<ResponseEntity<TransactionDto>> updateTransaction(
        @PathVariable Long transactionId, @Valid @RequestBody TransactionDto transactionDto) {
        if (transactionId == null || transactionId <= 0) {
            throw new InvalidPathVariableException(INVALID_TRANSACTION_ID + transactionId);
        }
        return transactionService.updateTransaction(transactionId, transactionDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Delete a transaction by ID
    @DeleteMapping("/{transactionId}")
    public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable Long transactionId) {
        if (transactionId == null || transactionId <= 0) {
            throw new InvalidPathVariableException(INVALID_TRANSACTION_ID + ": " + transactionId);
        }
        return transactionService.deleteTransaction(transactionId)
            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "/import/{budgetId}")
    public Mono<ResponseEntity<String>> startCsvImport(
        @PathVariable Long budgetId, @RequestPart("file") FilePart filePart) {
        log.debug("file import for budgetId " + budgetId);
        return transactionService.startCsvImport(budgetId, filePart)
            .map(importJobId -> new ResponseEntity<>(importJobId, HttpStatus.ACCEPTED))
            .onErrorResume(e -> Mono.just(new ResponseEntity<>("Failed to initiate import", HttpStatus.BAD_REQUEST)));
    }

}
