package com.hl.fambud.repository;

import com.hl.fambud.model.Transactor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactorRepository extends ReactiveCrudRepository<Transactor, Long> {

    Flux<Transactor> findByBudgetId(Long budgetId);

    Mono<Void> deleteByBudgetId(Long budgetId);
}
