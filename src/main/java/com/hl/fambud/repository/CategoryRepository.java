package com.hl.fambud.repository;

import com.hl.fambud.model.Category;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryRepository extends ReactiveCrudRepository<Category, Long> {

    Flux<Category> findByBudgetId(Long budgetId);

    Mono<Void> deleteByBudgetId(Long budgetId);
}
