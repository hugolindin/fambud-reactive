package com.hl.fambud.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.mapper.BudgetMapperImpl;
import com.hl.fambud.repository.BudgetRepository;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.util.TestDataGenerator;
import com.hl.fambud.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

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

    private ObjectMapper objectMapper;

    @BeforeEach
    public void init() {
        objectMapper = TestUtil.getObjectMapper();
    }

    @Test
    public void categorise() throws Exception {
        BudgetDto createdBudgetDto = TestUtil.postBudget(webTestClient, BudgetDto.builder().name("Budget").build());
        Long createdBudgetId = createdBudgetDto.getBudgetId();
        List<CategoryDto> categoryDtoList = TestDataGenerator.getCategoryDtosFromJsonFile();
        categoryDtoList.forEach(categoryDto -> {
            categoryDto.setCategoryId(null);
            categoryDto.setBudgetId(createdBudgetId);
            CategoryDto createdCategoryDto = TestUtil.postCategory(webTestClient, createdBudgetId, categoryDto);
            System.out.println("created category " + createdCategoryDto);
        });
        ClassPathResource transactionFileResource = new ClassPathResource("json/categorisation-test-transactions.json");
        List<TransactionDto> transactionDtoList = objectMapper.readValue(transactionFileResource.getFile(),
            new TypeReference<List<TransactionDto>>() {});
        BudgetMapper budgetMapper = new BudgetMapperImpl();
//        categoriser.categorise(createdBudgetId, budgetMapper.transactionDtoListToTransactionList(transactionDtoList),
//            mappingFileResource.getFile()).subscribe();
    }
}
