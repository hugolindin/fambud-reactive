package com.hl.fambud.util;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.dto.TransactorDto;
import com.hl.fambud.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

public class TestDataGenerator {

    public final static String BUDGET_BASE_URL = "/api/budgets";
    public final static String CATEGORY_BASE_URL = "/api/categories";
    public final static String TRANSACTION_BASE_URL = "/api/transactions";

    public static BudgetDto getBudgetDto() {
        return BudgetDto.builder()
            .budgetId(1L)
            .name("Family Monthly Budget")
            .categories(Arrays.asList(
                CategoryDto.builder()
                    .categoryId(1L)
                    .name("Groceries")
                    .build(),
                CategoryDto.builder()
                    .categoryId(2L)
                    .name("Utilities")
                    .build()
            ))
            .transactors(Arrays.asList(
                TransactorDto.builder()
                    .transactorId(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build(),
                TransactorDto.builder()
                    .transactorId(2L)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build()
            ))
            .transactions(Arrays.asList(
                TransactionDto.builder()
                    .transactionId(1L)
                    .description("Supermarket shopping")
                    .amount(BigDecimal.valueOf(150.00))
                    .date(LocalDate.now())
                    .type(TransactionType.EXPENSE)
                    .categoryId(1L)
                    .build(),
                TransactionDto.builder()
                    .transactionId(2L)
                    .description("Monthly Electricity Bill")
                    .amount(BigDecimal.valueOf(75.00))
                    .date(LocalDate.now().minusDays(5))
                    .type(TransactionType.EXPENSE)
                    .categoryId(2L)
                    .build(),
                TransactionDto.builder()
                    .transactionId(3L)
                    .description("Freelance Income")
                    .amount(BigDecimal.valueOf(500.00))
                    .date(LocalDate.now().minusDays(10))
                    .type(TransactionType.INCOME)
                    .categoryId(1L)
                    .build(),
                TransactionDto.builder()
                    .transactionId(4L)
                    .description("Internet Bill")
                    .amount(BigDecimal.valueOf(50.00))
                    .date(LocalDate.now().minusDays(2))
                    .type(TransactionType.EXPENSE)
                    .categoryId(2L)
                    .build()))
            .build();
    }

    public static void setIdsToNull(BudgetDto budgetDto) {
        budgetDto.setBudgetId(null);
        if (budgetDto.getCategories() != null)
            budgetDto.getCategories().forEach(categoryDto -> categoryDto.setCategoryId(null));
        if (budgetDto.getTransactors() != null)
                budgetDto.getTransactors().forEach(transactorDto -> transactorDto.setTransactorId(null));
        if (budgetDto.getTransactions() != null)
            budgetDto.getTransactions().forEach(transactionDto -> {
            transactionDto.setTransactionId(null);
            transactionDto.setCategoryId(null);
            transactionDto.setBudgetId(null);
        });
    }

    public static CategoryDto getCategoryDto() {
        return CategoryDto.builder()
            .budgetId(1L)
            .categoryId(1L)
            .name("Insurance")
            .build();
    }

    public static TransactionDto getTransactionDto() {
        return TransactionDto.builder()
            .transactorId(1L)
            .categoryId(1L)
            .budgetId(1L)
            .description("Initial Description")
            .amount(BigDecimal.valueOf(50.00))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .build();
    }

}
