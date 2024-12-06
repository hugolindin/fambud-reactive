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
        Map<Long, BigDecimal> categoryAmountMap = new HashMap<>();
        categoriser.categorise(createTestData(
            "json/categorisation-test-transactions.json", categoryAmountMap))
            .subscribe();
    }

    @Test
    public void categoryTransactionSummary() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        Map<Long, BigDecimal> categoryAmountMap = new HashMap<>();
        Long createdBudgetId = createTestData("json/categorised-transactions.json", categoryAmountMap);
        PeriodSummaryDto summary = categoriser.summariseCategoryTransactions(createdBudgetId, startDate, endDate).block();
        log.debug(objectMapper.writeValueAsString(summary));
        log.debug(objectMapper.writeValueAsString(categoryAmountMap));
        List<CategorySummaryDto> categorySummaries = summary.getCategories();
        categorySummaries.forEach(categorySummary -> {
            BigDecimal expectedAmount = categoryAmountMap.get(categorySummary.getCategoryId());
            BigDecimal calculatedAmount = categorySummary.getAmount();
            log.debug("expected " + expectedAmount + " calculated " + calculatedAmount);
            assertEquals(expectedAmount, calculatedAmount);
        });
    }

    private Long createTestData(String transactionDataFileName, Map<Long, BigDecimal> categoryAmountMap) throws IOException {
        BudgetDto createdBudgetDto = TestUtil.postBudget(webTestClient, BudgetDto.builder().name("Budget").build());
        Long createdBudgetId = createdBudgetDto.getBudgetId();
        List<CategoryDto> categoryDtoList = TestDataGenerator.getCategoryDtosFromJsonFile();
        List<Long> categoryIds = new ArrayList<>();
        categoryDtoList.forEach(categoryDto -> {
            categoryDto.setCategoryId(null);
            categoryDto.setBudgetId(createdBudgetId);
            CategoryDto createdCategoryDto = TestUtil.postCategory(webTestClient, createdBudgetId, categoryDto);
            log.trace("created category " + createdCategoryDto);
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
            Transaction savedTransaction = transactionRepository.save(budgetMapper.toTransaction(transactionDto)).block();
            log.trace("created transaction " + savedTransaction);
            Long categoryId = transactionDto.getCategoryId();
            BigDecimal amount = transactionDto.getAmount();
            if (categoryAmountMap.containsKey(categoryId)) {
                categoryAmountMap.put(categoryId, categoryAmountMap.get(categoryId).add(amount));
            } else {
                categoryAmountMap.put(categoryId, amount);
            }
        });
        return createdBudgetId;
    }
}
