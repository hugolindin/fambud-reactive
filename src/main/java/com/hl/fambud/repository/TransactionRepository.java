package com.hl.fambud.repository;

import com.hl.fambud.model.Transaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {

    Flux<Transaction> findByBudgetId(Long budgetId);

    Flux<Transaction> findByTransactorId(Long transactorId);

    Mono<Void> deleteByBudgetId(Long budgetId);

    Mono<Void> deleteByTransactorId(Long transactorId);
}
