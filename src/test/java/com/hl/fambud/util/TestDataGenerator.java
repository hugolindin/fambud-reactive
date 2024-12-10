package com.hl.fambud.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.dto.TransactorDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.mapper.BudgetMapperImpl;
import com.hl.fambud.model.TransactionType;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class TestDataGenerator {

    public final static String BUDGET_BASE_URL = "/api/budgets";
    public final static String BUDGET_ID_URL = BUDGET_BASE_URL + "/{budgetId}";
    public final static String BUDGET_SUMMARY_URL = BUDGET_ID_URL + "/summaries";
    public final static String CATEGORY_BASE_URL = BUDGET_ID_URL + "/categories";
    public final static String CATEGORY_ID_URL = CATEGORY_BASE_URL + "/{categoryId}";
    public final static String TRANSACTION_BASE_URL = BUDGET_ID_URL + "/transactions";
    public final static String TRANSACTION_ID_URL = TRANSACTION_BASE_URL + "/{transactionId}";
    public final static Long TEST_BUDGET_ID = 1L;
    public final static Long TEST_CATEGORY_ID = 1L;
    public final static Long TEST_CATEGORY_ID_2 = 2L;
    public final static Long TEST_TRANSACTION_ID = 1L;
    public final static Long TEST_TRANSACTION_ID_2 = 2L;
    public final static Long TEST_TRANSACTION_ID_3 = 3L;
    public final static Long TEST_TRANSACTION_ID_4 = 4L;
    public final static Long TEST_TRANSACTOR_ID = 1L;
    public final static Long TEST_TRANSACTOR_ID_2 = 2L;

    public static BudgetDto getBudgetDto() {
        return BudgetDto.builder()
            .budgetId(TEST_BUDGET_ID)
            .name("Family Monthly Budget")
            .categories(Arrays.asList(
                CategoryDto.builder()
                    .categoryId(TEST_CATEGORY_ID)
                    .name("Groceries")
                    .build(),
                CategoryDto.builder()
                    .categoryId(TEST_CATEGORY_ID_2)
                    .name("Utilities")
                    .build()
            ))
            .transactors(Arrays.asList(
                TransactorDto.builder()
                    .transactorId(TEST_TRANSACTOR_ID)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john.doe@example.com")
                    .build(),
                TransactorDto.builder()
                    .transactorId(TEST_TRANSACTOR_ID_2)
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@example.com")
                    .build()
            ))
            .transactions(Arrays.asList(
                TransactionDto.builder()
                    .transactionId(TEST_TRANSACTION_ID)
                    .description("Supermarket shopping")
                    .amount(BigDecimal.valueOf(150.00))
                    .date(LocalDate.now())
                    .type(TransactionType.EXPENSE)
                    .categoryId(TEST_CATEGORY_ID)
                    .build(),
                TransactionDto.builder()
                    .transactionId(TEST_TRANSACTION_ID_2)
                    .description("Monthly Electricity Bill")
                    .amount(BigDecimal.valueOf(75.00))
                    .date(LocalDate.now().minusDays(5))
                    .type(TransactionType.EXPENSE)
                    .categoryId(TEST_CATEGORY_ID_2)
                    .build(),
                TransactionDto.builder()
                    .transactionId(TEST_TRANSACTION_ID_3)
                    .description("Freelance Income")
                    .amount(BigDecimal.valueOf(500.00))
                    .date(LocalDate.now().minusDays(10))
                    .type(TransactionType.INCOME)
                    .categoryId(TEST_CATEGORY_ID)
                    .build(),
                TransactionDto.builder()
                    .transactionId(TEST_TRANSACTION_ID_4)
                    .description("Internet Bill")
                    .amount(BigDecimal.valueOf(50.00))
                    .date(LocalDate.now().minusDays(2))
                    .type(TransactionType.EXPENSE)
                    .categoryId(TEST_CATEGORY_ID_2)
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
            .budgetId(TEST_BUDGET_ID)
            .categoryId(TEST_CATEGORY_ID)
            .name("Insurance")
            .build();
    }

    public static List<CategoryDto> getCategoryDtosFromJsonFile() throws IOException {
        ClassPathResource categoryFileResource = new ClassPathResource("json/saved-categories.json");
        BudgetMapper budgetMapper = new BudgetMapperImpl();
        return TestUtil.getObjectMapper().readValue(categoryFileResource.getFile(),
            new TypeReference<List<CategoryDto>>() {});
    }

    public static TransactionDto getTransactionDto() {
        return TransactionDto.builder()
            .transactorId(TEST_TRANSACTOR_ID)
            .categoryId(TEST_CATEGORY_ID)
            .budgetId(TEST_BUDGET_ID)
            .description("Initial Description")
            .amount(BigDecimal.valueOf(50.00))
            .date(LocalDate.now())
            .type(TransactionType.EXPENSE)
            .build();
    }

}
