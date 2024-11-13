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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
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

    @Autowired
    private DatabaseClient databaseClient;

    @SneakyThrows
    public Mono<BudgetDto> createBudget(@Valid BudgetDto budgetDto) {
        Budget budget = budgetMapper.toBudget(budgetDto);
        log.debug("createBudget " + objectMapper.writeValueAsString(budget));
        return saveBudgetAndNestedObjects(budget);
    }

    private Mono<BudgetDto> saveBudgetAndNestedObjects(Budget budget) {
        return budgetRepository.save(budget)
            .flatMap(savedBudget -> {
                log.debug("saved budget " + savedBudget);
                // Update the budgetId for categories
                budget.getCategories().forEach(category -> {
                    log.debug("set category budget id " + category);
                    category.setBudgetId(savedBudget.getBudgetId());
                });
                // Update the budgetId for transactors
                budget.getTransactors().forEach(transactor -> {
                    log.debug("set transactor budget id " + transactor);
                    transactor.setBudgetId(savedBudget.getBudgetId());
                });
                // save categories and transactors
                Mono<List<Category>> savedCategoriesMono =
                    categoryRepository.saveAll(budget.getCategories()).collectList();
                Mono<List<Transactor>> savedTransactorsMono =
                    transactorRepository.saveAll(budget.getTransactors()).collectList();
                // Save transactions for each transactor after saving transactors
                Mono<List<Transaction>> savedTransactionsMono = savedTransactorsMono
                    .flatMapMany(Flux::fromIterable)
                    .flatMap(transactor -> {
                        // Set the correct transactorId for each transaction
                        return Flux.fromIterable(transactor.getTransactions())
                            .doOnNext(transaction -> {
                                transaction.setTransactorId(transactor.getTransactorId());
                                log.debug("Preparing transaction for save: " + transaction);
                            });
                    })
                    .collectList()
                    .flatMapMany(transactionRepository::saveAll)
                    .collectList();
                // Combine all saved entities and prepare the final BudgetDto
                return Mono.zip(savedCategoriesMono, savedTransactorsMono, savedTransactionsMono)
                    .flatMap(tuple -> {
                        List<Category> savedCategories = tuple.getT1();
                        List<Transactor> savedTransactors = tuple.getT2();
                        List<Transaction> savedTransactions = tuple.getT3();

                        log.debug("Categories saved: " + savedCategories.size());
                        log.debug("Transactors saved: " + savedTransactors.size());
                        log.debug("Transactions saved: " + savedTransactions.size());

                        savedBudget.setCategories(savedCategories);
                        savedBudget.setTransactors(savedTransactors);

                        return Mono.just(savedBudget);
                    })
                    .map(budgetMapper::toBudgetDto);
            });
    }

    public Mono<BudgetDto> getBudget(Long budgetId) {
        return budgetRepository.findById(budgetId)
            .flatMap(this::loadNestedObjects)
            .map(budgetMapper::toBudgetDto)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Budget not found with id: " + budgetId)));
    }

    private Mono<Budget> loadNestedObjects(Budget budget) {
        // Fetch categories associated with the budget
        Mono<List<Category>> categoriesMono = categoryRepository.findByBudgetId(budget.getBudgetId()).collectList();

        // Fetch transactors associated with the budget
        Mono<List<Transactor>> transactorsMono = transactorRepository.findByBudgetId(budget.getBudgetId()).collectList();

        // Fetch categories and transactors concurrently
        return Mono.zip(categoriesMono, transactorsMono)
            .flatMap(tuple -> {
                List<Category> categories = tuple.getT1();
                List<Transactor> transactors = tuple.getT2();
                log.debug("Categories loaded: " + categories);
                log.debug("Transactors loaded: " + transactors);

                // Fetch transactions for each transactor reactively and enrich each transactor
                return Flux.fromIterable(transactors)
                    .flatMap(transactor -> transactionRepository.findByTransactorId(transactor.getTransactorId())
                        .collectList()
                        .map(transactions -> {
                            transactor.setTransactions(transactions);
                            return transactor;
                        })
                    )
                    .collectList()
                    .map(finalTransactors -> {
                        // Assign the fetched and enriched lists back to the budget
                        budget.setCategories(categories);
                        budget.setTransactors(finalTransactors);
                        return budget;
                    });
            });
    }


}
