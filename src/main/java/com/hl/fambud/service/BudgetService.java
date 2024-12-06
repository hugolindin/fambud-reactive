package com.hl.fambud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.model.Budget;
import com.hl.fambud.model.Category;
import com.hl.fambud.model.Transaction;
import com.hl.fambud.model.Transactor;
import com.hl.fambud.repository.BudgetRepository;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.repository.TransactorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactorRepository transactorRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetMapper budgetMapper;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Mono<BudgetDto> createBudget(@Valid BudgetDto budgetDto) {
        Budget budget = budgetMapper.toBudget(budgetDto);
        log.debug("createBudget " + objectMapper.writeValueAsString(budget));
        return saveBudgetAndNestedObjects(budget);
    }

    public Mono<BudgetDto> getBudget(Long budgetId) {
        return budgetRepository.findById(budgetId)
            .flatMap(this::loadNestedObjects)
            .map(budgetMapper::toBudgetDto)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Budget not found with id: " + budgetId)));
    }

    public Flux<BudgetDto> getAllBudgets() {
        return budgetRepository.findAll()
            .flatMap(this::loadNestedObjects)
            .doOnError(exception -> log.error("Unable to get all budgets ", exception))
            .map(budgetMapper::toBudgetDto);
    }

    public Mono<BudgetDto> updateBudget(Long budgetId, @Valid BudgetDto budgetDto) {
        return budgetRepository.findById(budgetId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Budget not found with ID " + budgetId)))
            .flatMap(retrievedBudget -> {
                log.debug("budget retrieved from DB " + retrievedBudget);
                budgetMapper.updateBudgetFromDto(budgetDto, retrievedBudget);
                log.debug("retrieved budget updated with new data " + retrievedBudget);
                return saveBudgetAndNestedObjects(retrievedBudget);
            });
    }

    public Mono<Void> deleteBudget(Long budgetId) {
        Mono<Void> transactions = transactionRepository.deleteByBudgetId(budgetId);
        Mono<Void> transactors = transactorRepository.deleteByBudgetId(budgetId);
        Mono<Void> categories = categoryRepository.deleteByBudgetId(budgetId);
        Mono<Void> budget = budgetRepository.deleteById(budgetId);
        return transactions.then(transactors).then(categories).then(budget);
    }

    private Mono<BudgetDto> saveBudgetAndNestedObjects(Budget budget) {
        return budgetRepository.save(budget)
            .flatMap(savedBudget -> {
                log.debug("Saved budget: " + savedBudget);

                // Update the budgetId for categories and save them reactively
                Flux<Category> updatedCategoriesFlux = budget.getCategories() != null
                    ? Flux.fromIterable(budget.getCategories())
                        .map(category -> {
                            category.setBudgetId(savedBudget.getBudgetId());
                            log.debug("Prepared category for save: " + category);
                            return category;
                        })
                    : Flux.empty();

                Mono<List<Category>> savedCategoriesMono = updatedCategoriesFlux
                    .flatMap(categoryRepository::save)
                    .collectList()
                    .map(categories -> {
                        log.debug("Saved categories: " + categories.size());
                        return categories;
                    })
                    .share();

                // Update the budgetId for transactors and save them reactively
                Flux<Transactor> updatedTransactorsFlux = budget.getTransactors() != null
                    ? Flux.fromIterable(budget.getTransactors())
                        .map(transactor -> {
                            transactor.setBudgetId(savedBudget.getBudgetId());
                            log.debug("Prepared transactor for save: " + transactor);
                            return transactor;
                        })
                    : Flux.empty();

                Mono<List<Transactor>> savedTransactorsMono = updatedTransactorsFlux
                    .flatMap(transactorRepository::save)
                    .collectList()
                    .map(transactors -> {
                        log.debug("Saved transactors: " + transactors.size());
                        return transactors;
                    })
                    .share();

                // Update the budgetId for transactions and save them reactively
                Flux<Transaction> updatedTransactionsFlux = budget.getTransactions() != null
                    ? Flux.fromIterable(budget.getTransactions())
                        .map(transaction -> {
                            transaction.setBudgetId(savedBudget.getBudgetId());
                            log.debug("Prepared transaction for save: " + transaction);
                            return transaction;
                        })
                    : Flux.empty();

                Mono<List<Transaction>> savedTransactionsMono = updatedTransactionsFlux
                    .flatMap(transactionRepository::save)
                    .collectList()
                    .map(transactions -> {
                        log.debug("Saved transactions: " + transactions);
                        return transactions;
                    });
                // Combine all saved entities and return the full budget using getBudget
                return Mono.when(savedCategoriesMono, savedTransactorsMono, savedTransactionsMono)
                    .then(getBudget(savedBudget.getBudgetId()));
            });
    }

    private Mono<Budget> loadNestedObjects(Budget budget) {
        Mono<List<Category>> categoriesMono =
            categoryRepository.findByBudgetId(budget.getBudgetId()).collectList();
        Mono<List<Transactor>> transactorsMono =
            transactorRepository.findByBudgetId(budget.getBudgetId()).collectList();
        Mono<List<Transaction>> transactionsMono =
            transactionRepository.findByBudgetId(budget.getBudgetId()).collectList();
        // Fetch categories, transactions and transactors concurrently
        return Mono.zip(categoriesMono, transactorsMono, transactionsMono)
            .flatMap(tuple -> {
                List<Category> categories = tuple.getT1();
                List<Transactor> transactors = tuple.getT2();
                List<Transaction> transactions = tuple.getT3();
                log.debug("Categories loaded: " + categories);
                log.debug("Transactors loaded: " + transactors);
                log.debug("Transactions loaded: " + transactions);
                budget.setCategories(categories);
                budget.setTransactors(transactors);
                budget.setTransactions(transactions);
                return Mono.just(budget);
            });
    }

}
