package com.hl.fambud.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.dto.reporting.CategorySummaryDto;
import com.hl.fambud.dto.reporting.PeriodSummaryDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.mapper.BudgetMapperImpl;
import com.hl.fambud.model.Transaction;
import com.hl.fambud.model.TransactionType;
import com.hl.fambud.repository.BudgetRepository;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.service.TransactionCategoriser;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureWebTestClient
@Slf4j
public class CategoriserIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoriser categoriser;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper = TestUtil.getObjectMapper();
    }

    @Test
    @Disabled
    public void categorise() throws Exception {
        Map<String, BigDecimal> categoryAmountMap = new HashMap<>();
        categoriser.categorise(createTestData(
            "json/categorisation-test-transactions.json", categoryAmountMap))
            .block();
    }

    @Test
    public void categoryTransactionSummary() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Map<String, BigDecimal> categoryAmountMap = new HashMap<>();
        Long createdBudgetId = createTestData(
            "json/categorised-transactions.json", categoryAmountMap);
        PeriodSummaryDto summary = categoriser.getBudgetPeriodSummary(createdBudgetId, startDate, endDate)
            .block();
        log.debug(objectMapper.writeValueAsString(summary));
        log.debug(objectMapper.writeValueAsString(categoryAmountMap));
        List<CategorySummaryDto> expenseSummaries = summary.getExpenseCategories();
        expenseSummaries.forEach(categorySummary -> {
            var categorySummaryKey = TransactionType.EXPENSE + "-" + categorySummary.getCategoryId();
            BigDecimal expectedAmount = categoryAmountMap.get(categorySummaryKey);
            BigDecimal calculatedAmount = categorySummary.getAmount();
            log.debug(categorySummaryKey + " expected " + expectedAmount + " calculated " + calculatedAmount);
            assertEquals(expectedAmount, calculatedAmount);
        });
        List<CategorySummaryDto> incomeSummaries = summary.getIncomeCategories();
        incomeSummaries.forEach(categorySummary -> {
            var categorySummaryKey = TransactionType.INCOME + "-" + categorySummary.getCategoryId();
            BigDecimal expectedAmount = categoryAmountMap.get(categorySummaryKey);
            BigDecimal calculatedAmount = categorySummary.getAmount();
            log.debug(categorySummaryKey + " expected " + expectedAmount + " calculated " + calculatedAmount);
            assertEquals(expectedAmount, calculatedAmount);
        });
    }

    private Long createTestData(String transactionDataFileName, Map<String, BigDecimal> categoryAmountMap) throws IOException {
        BudgetDto createdBudgetDto = TestUtil.postBudget(webTestClient, BudgetDto.builder().name("Budget").build());
        Long createdBudgetId = createdBudgetDto.getBudgetId();
        List<CategoryDto> categoryDtoList = TestDataGenerator.getCategoryDtosFromJsonFile();
        List<Long> categoryIds = new ArrayList<>();
        categoryDtoList.forEach(categoryDto -> {
            categoryDto.setCategoryId(null);
            categoryDto.setBudgetId(createdBudgetId);
            CategoryDto createdCategoryDto = TestUtil.postCategory(webTestClient, createdBudgetId, categoryDto);
            log.debug("created category " + createdCategoryDto);
            categoryIds.add(createdCategoryDto.getCategoryId());
        });
        log.debug("categoryIds " + categoryIds);
        ClassPathResource transactionFileResource = new ClassPathResource(transactionDataFileName);
        List<TransactionDto> transactionDtoList = objectMapper.readValue(transactionFileResource.getFile(),
            new TypeReference<List<TransactionDto>>() {});
        BudgetMapper budgetMapper = new BudgetMapperImpl();
        Random random = new Random();
        transactionDtoList.forEach(transactionDto -> {
            transactionDto.setBudgetId(createdBudgetId);
            transactionDto.setCategoryId(categoryIds.get(random.nextInt(categoryIds.size())));
            transactionDto.setTransactionId(null);
            Transaction savedTransaction = transactionRepository.save(budgetMapper.toTransaction(transactionDto))
                .block();
            log.trace("created transaction " + savedTransaction);
            Long categoryId = transactionDto.getCategoryId();
            BigDecimal amount = transactionDto.getAmount();
            TransactionType transactionType = transactionDto.getType();
            var mapKey = transactionType + "-" + categoryId;
            if (categoryAmountMap.containsKey(mapKey)) {
                categoryAmountMap.put(mapKey, categoryAmountMap.get(mapKey).add(amount));
            } else {
                categoryAmountMap.put(mapKey, amount);
            }
        });
        return createdBudgetId;
    }
}
