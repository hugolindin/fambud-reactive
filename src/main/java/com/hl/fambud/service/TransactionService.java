package com.hl.fambud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.model.Transaction;
import com.hl.fambud.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;
    private final ObjectMapper objectMapper;
    private final Scheduler parallelScheduler = Schedulers.parallel();

    @SneakyThrows
    public Mono<TransactionDto> createTransaction(@Valid TransactionDto transactionDto) {
        Transaction transaction = budgetMapper.toTransaction(transactionDto);
        log.debug("createTransaction " + objectMapper.writeValueAsString(transaction));
        return transactionRepository.save(transaction)
            .map(budgetMapper::transactionToTransactionDto);
    }

    public Mono<TransactionDto> getTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .map(budgetMapper::transactionToTransactionDto)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Transaction not found with id: " + transactionId)));
    }

    public Flux<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
            .doOnError(exception -> log.error("Unable to get all transactions", exception))
            .map(budgetMapper::transactionToTransactionDto);
    }

    public Mono<TransactionDto> updateTransaction(Long transactionId, @Valid TransactionDto transactionDto) {
        return transactionRepository.findById(transactionId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Transaction not found with ID " + transactionId)))
            .flatMap(retrievedTransaction -> {
                log.debug("Transaction retrieved from DB: " + retrievedTransaction);
                budgetMapper.updateTransactionFromTransactionDto(transactionDto, retrievedTransaction);
                log.debug("Retrieved transaction updated with new data: " + retrievedTransaction);
                return transactionRepository.save(retrievedTransaction)
                    .map(budgetMapper::transactionToTransactionDto);
            });
    }

    public Mono<Void> deleteTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Transaction not found with ID " + transactionId)))
            .flatMap(transaction -> transactionRepository.deleteById(transactionId))
            .then();
    }

    public Mono<String> startCsvImport(Long budgetId, FilePart filePart) {
        String importJobId = UUID.randomUUID().toString();
        log.info("Import job {} started with status: IN_PROGRESS", importJobId);
        return transactionRepository.findByBudgetId(budgetId)
            .collectList()
            .map(existingTransactions -> {
                Set<String> uniqueTransactionIdentifiers = existingTransactions.stream()
                    .map(TransactionUtil::buildTransactionIdentifier)
                    .collect(Collectors.toSet());
                log.debug("Existing transaction identifiers: " + uniqueTransactionIdentifiers);
                return uniqueTransactionIdentifiers;
            })
            .flatMapMany(uniqueTransactionIdentifiers ->
                TransactionUtil.convertToMultipartFile(Schedulers.boundedElastic(), filePart)
                    .flatMapMany(multipartFile -> TransactionUtil.parseCsvFile(Schedulers.boundedElastic(), budgetId, multipartFile))
                    .filter(transaction -> {
                        String identifier = TransactionUtil.buildTransactionIdentifier(transaction);
                        log.debug("transaction identifier " + identifier);
                        boolean isDuplicate = uniqueTransactionIdentifiers.contains(identifier);
                        if (!isDuplicate) {
                            uniqueTransactionIdentifiers.add(identifier); // Add to set to avoid duplicates
                            return true;
                        }
                        log.debug("Duplicate transaction skipped: " + transaction);
                        return false;
                    })
                    .flatMap(transactionRepository::save)
            )
            .subscribeOn(Schedulers.boundedElastic())
            .then(Mono.fromRunnable(() -> log.info("Import job {} completed with status: COMPLETED", importJobId)))
            .onErrorResume(e -> {
                log.error("Import job {} failed with status: FAILED", importJobId, e);
                return Mono.empty();
            })
            .thenReturn(importJobId);
    }

}
