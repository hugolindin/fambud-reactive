package com.hl.fambud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.model.Category;
import com.hl.fambud.repository.CategoryRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    private DatabaseClient databaseClient;

    @SneakyThrows
    public Mono<CategoryDto> createCategory(@Valid CategoryDto categoryDto) {
        Category category = budgetMapper.toCategory(categoryDto);
        log.debug("createCategory " + objectMapper.writeValueAsString(category));
        return categoryRepository.save(category)
            .map(budgetMapper::categoryToCategoryDto);
    }

    public Mono<CategoryDto> getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .map(budgetMapper::categoryToCategoryDto)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Category not found with id: " + categoryId)));
    }

    public Flux<CategoryDto> getAllCategoriesForBudget(Long budgetId) {
        return categoryRepository.findByBudgetId(budgetId)
            .doOnError(exception -> log.error("Unable to get all categories", exception))
            .map(budgetMapper::categoryToCategoryDto);
    }

    public Mono<CategoryDto> updateCategory(Long categoryId, @Valid CategoryDto categoryDto) {
        return categoryRepository.findById(categoryId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Category not found with ID " + categoryId)))
            .flatMap(retrievedCategory -> {
                log.debug("Category retrieved from DB: " + retrievedCategory);
                budgetMapper.updateCategoryFromDto(categoryDto, retrievedCategory);
                log.debug("Retrieved category updated with new data: " + retrievedCategory);
                return categoryRepository.save(retrievedCategory)
                    .map(budgetMapper::categoryToCategoryDto);
            });
    }

    public Mono<Void> deleteCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .switchIfEmpty(Mono.error(new EntityNotFoundException("Category not found with ID " + categoryId)))
            .flatMap(category -> categoryRepository.deleteById(categoryId))
            .then();
    }
}
