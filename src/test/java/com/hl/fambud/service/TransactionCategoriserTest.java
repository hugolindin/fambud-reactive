package com.hl.fambud.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.dto.TransactionDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.mapper.BudgetMapperImpl;
import com.hl.fambud.model.Category;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import com.hl.fambud.util.TestDataGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class TransactionCategoriserTest {

    private ObjectMapper objectMapper;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionCategoriser categoriser;

    private final static Long BUDGET_ID = 16L;

    @BeforeEach
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    public void categoriseTransactions() throws Exception {
        List<CategoryDto> categoryDtoList = TestDataGenerator.getCategoryDtosFromJsonFile();
        BudgetMapper budgetMapper = new BudgetMapperImpl();
        List<Category> categoryList = budgetMapper.categoryDtoListToCategoryList(categoryDtoList);
        System.out.println("categories: " + categoryList);
        when(categoryRepository.findByBudgetId(BUDGET_ID)).thenReturn(Flux.fromIterable(categoryList));
        try {
            ClassPathResource mappingFileResource = new ClassPathResource("transaction-category-mapping-241121.csv");
            ClassPathResource transactionFileResource = new ClassPathResource("json/saved-transactions.json");
            List<TransactionDto> transactionDtoList = objectMapper.readValue(transactionFileResource.getFile(),
                new TypeReference<List<TransactionDto>>() {});
            categoriser.categorise(BUDGET_ID, budgetMapper.transactionDtoListToTransactionList(transactionDtoList),
                mappingFileResource.getFile()).subscribe();
        } catch (IOException e) {
            throw new RuntimeException("Error reading transactions from JSON file", e);
        }
    }
}
