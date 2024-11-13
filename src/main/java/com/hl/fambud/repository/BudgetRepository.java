package com.hl.fambud.repository;

import com.hl.fambud.model.Budget;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface BudgetRepository extends ReactiveCrudRepository<Budget, Long> {
}
