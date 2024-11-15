package com.hl.fambud.controller;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.service.BudgetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("${app.base-url}/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public Mono<ResponseEntity<BudgetDto>> createBudget(@Valid @RequestBody BudgetDto budgetDto) {
        return budgetService.createBudget(budgetDto)
            .map(budget -> new ResponseEntity<>(budget, HttpStatus.CREATED));
    }

    @GetMapping("/{budgetId}")
    public Mono<ResponseEntity<BudgetDto>> getBudget(@PathVariable Long budgetId) {
        /*if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + ": " + budgetId);
        }*/
        return budgetService.getBudget(budgetId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<BudgetDto> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @PutMapping("/{budgetId}")
    public Mono<ResponseEntity<BudgetDto>> updateBudget(
        @PathVariable Long budgetId, @Valid @RequestBody BudgetDto budgetDto) {
        /*if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + ": " + budgetId);
        }*/
        return budgetService.updateBudget(budgetId, budgetDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{budgetId}")
    public Mono<ResponseEntity<Void>> deleteBudget(@PathVariable Long budgetId) {
        /*if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + ": " + budgetId);
        }*/
        return budgetService.deleteBudget(budgetId)
            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
