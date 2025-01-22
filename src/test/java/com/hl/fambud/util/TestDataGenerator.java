package com.hl.fambud.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.dto.TransactorDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.mapper.BudgetMapperImpl;
import com.hl.fambud.model.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    public final static String BUDGET_BASE_URL = "/api/budgets";
    public final static String BUDGET_ID_URL = BUDGET_BASE_URL + "/{budgetId}";
    public final static String BUDGET_SUMMARY_URL = BUDGET_ID_URL + "/summaries";
    public final static String CATEGORY_BASE_URL = BUDGET_ID_URL + "/categories";
    public final static String CATEGORY_ID_URL = CATEGORY_BASE_URL + "/{categoryId}";
    public final static String TRANSACTION_BASE_URL = BUDGET_ID_URL + "/transactions";
    public final static String TRANSACTION_ID_URL = TRANSACTION_BASE_URL + "/{transactionId}";
    public final static String TRANSACTION_CATEGORIES_URL = TRANSACTION_BASE_URL + "/categories";
    private static final Logger LOG = LoggerFactory.getLogger(TestDataGenerator.class);

    public static BudgetDto getBudgetDto() {
        return BudgetDto.builder()
            .name("Family Monthly Budget")
            .categories(Arrays.asList(
                CategoryDto.builder()
                    .name("Groceries")
                    .build(),
                CategoryDto.builder()
                    .name("Utilities")
                    .build()
            ))
            .transactors(Arrays.asList(
                TransactorDto.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build(),
                TransactorDto.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build()
            ))
            .transactions(Arrays.asList(
                TransactionDto.builder()
                    .description("Supermarket shopping")
                    .amount(BigDecimal.valueOf(150.00))
                    .date(LocalDate.now())
                    .type(TransactionType.EXPENSE)
                    .build(),
                TransactionDto.builder()
                    .description("Monthly Electricity Bill")
                    .amount(BigDecimal.valueOf(75.00))
                    .date(LocalDate.now().minusDays(5))
                    .type(TransactionType.EXPENSE)
                    .build(),
                TransactionDto.builder()
                    .description("Freelance Income")
                    .amount(BigDecimal.valueOf(500.00))
                    .date(LocalDate.now().minusDays(10))
                    .type(TransactionType.INCOME)
                    .build(),
                TransactionDto.builder()
                    .description("Internet Bill")
                    .amount(BigDecimal.valueOf(50.00))
                    .date(LocalDate.now().minusDays(2))
                    .type(TransactionType.EXPENSE)
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

    public static CategoryDto getCategoryDto(long budgetId) {
        return CategoryDto.builder()
            .budgetId(budgetId)
            .name("Insurance")
            .build();
    }

    public static List<CategoryDto> getCategoryDtosFromJsonFile() throws IOException {
        ClassPathResource categoryFileResource = new ClassPathResource("json/saved-categories.json");
        BudgetMapper budgetMapper = new BudgetMapperImpl();
        return TestUtil.getObjectMapper().readValue(categoryFileResource.getFile(),
            new TypeReference<List<CategoryDto>>() {});
    }

    public static TransactionDto getTransactionDto(long budgetId, long categoryId) {
        return TransactionDto.builder()
            .categoryId(categoryId)
            .description("Initial Description")
            .amount(BigDecimal.valueOf(50.00))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .build();
    }

    private static long getNextId() {
        return ThreadLocalRandom.current().nextLong(1, 101);
    }

}
