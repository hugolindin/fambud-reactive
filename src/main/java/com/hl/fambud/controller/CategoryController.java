package com.hl.fambud.controller;

import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.exception.InvalidPathVariableException;
import com.hl.fambud.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.hl.fambud.util.BudgetUtil.INVALID_CATEGORY_ID;

@RestController
@AllArgsConstructor
@RequestMapping("${app.base-url}/{budgetId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Mono<ResponseEntity<CategoryDto>> createCategory(@PathVariable Long budgetId, @Valid @RequestBody CategoryDto categoryDto) {
        categoryDto.setBudgetId(budgetId);
        return categoryService.createCategory(categoryDto)
            .map(category -> new ResponseEntity<>(category, HttpStatus.CREATED));
    }

    @GetMapping("/{categoryId}")
    public Mono<ResponseEntity<CategoryDto>> getCategory(@PathVariable Long categoryId) {
        return categoryService.getCategory(categoryId)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Flux<CategoryDto> getAllCategories(@PathVariable Long budgetId) {
        return categoryService.getAllCategoriesForBudget(budgetId);
    }

    @PutMapping("/{categoryId}")
    public Mono<ResponseEntity<CategoryDto>> updateCategory(
        @PathVariable Long categoryId, @Valid @RequestBody CategoryDto categoryDto) {
        if (categoryId == null || categoryId <= 0) {
            throw new InvalidPathVariableException(INVALID_CATEGORY_ID + categoryId);
        }
        return categoryService.updateCategory(categoryId, categoryDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{categoryId}")
    public Mono<ResponseEntity<Void>> deleteCategory(@PathVariable Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new InvalidPathVariableException(INVALID_CATEGORY_ID + ": " + categoryId);
        }
        return categoryService.deleteCategory(categoryId)
            .then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
