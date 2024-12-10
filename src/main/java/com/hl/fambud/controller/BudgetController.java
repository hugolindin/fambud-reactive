package com.hl.fambud.controller;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.exception.InvalidPathVariableException;
import com.hl.fambud.service.BudgetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.hl.fambud.util.BudgetUtil.INVALID_BUDGET_ID;

@RestController
@AllArgsConstructor
@RequestMapping("${app.base-url}")
public class BudgetController {

    private static final Logger log = LoggerFactory.getLogger(BudgetController.class);
    private final BudgetService budgetService;

    @PostMapping
    public Mono<ResponseEntity<BudgetDto>> createBudget(@Valid @RequestBody BudgetDto budgetDto) {
        log.info("creating budget " + budgetDto);
        return budgetService.createBudget(budgetDto)
            .map(budget -> new ResponseEntity<>(budget, HttpStatus.CREATED))
            .onErrorResume(exception -> {
                log.error("Error while creating budget", exception);
                return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
            });
    }

    @GetMapping("/{budgetId}")
    public Mono<ResponseEntity<BudgetDto>> getBudget(@PathVariable Long budgetId) {
        /*if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + ": " + budgetId);
        }*/
        log.info("getting budget " + budgetId);
        return budgetService.getBudget(budgetId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<BudgetDto> getAllBudgets() {
        log.info("getting all budgets");
        return budgetService.getAllBudgets();
    }

    @PutMapping("/{budgetId}")
    public Mono<ResponseEntity<BudgetDto>> updateBudget(
        @PathVariable Long budgetId, @Valid @RequestBody BudgetDto budgetDto) {
        if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + budgetId);
        }
        log.info("updating budget " + budgetDto);
        return budgetService.updateBudget(budgetId, budgetDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{budgetId}")
    public Mono<ResponseEntity<Void>> deleteBudget(@PathVariable Long budgetId) {
        if (budgetId == null || budgetId <= 0) {
            throw new InvalidPathVariableException(INVALID_BUDGET_ID + ": " + budgetId);
        }
        log.info("deleting budget " + budgetId);
        return budgetService.deleteBudget(budgetId)
            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
