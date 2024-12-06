package com.hl.fambud.service;

import com.hl.fambud.dto.reporting.CategorySummaryDto;
import com.hl.fambud.dto.reporting.PeriodSummaryDto;
import com.hl.fambud.mapper.BudgetMapper;
import com.hl.fambud.model.Category;
import com.hl.fambud.model.Transaction;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCategoriser {

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;

    private final BudgetMapper budgetMapper;

    private Map<String, String> loadMappingFile(File mappingFile) {
        Map<String, String> resultMap = new ConcurrentHashMap<>();
        try (Reader reader = new FileReader(mappingFile);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)) {
            // Load records in parallel
            csvParser.getRecords().parallelStream().forEach(record -> {
                String key = record.get(0);
                String value = record.get(1);
                resultMap.put(key, value);
            });
        } catch (IOException e) {
            throw new RuntimeException("Error reading mapping file", e);
        }
        return resultMap;
    }

    public Mono<Void> categorise(Long budgetId) {
        log.debug("categorise transactions for budget " + budgetId);
        return Mono.fromCallable(() -> {
            ClassPathResource mappingFileResource = new ClassPathResource("transaction-category-mapping-241121.csv");
            return mappingFileResource.getFile();
        })
        .onErrorResume(e -> {
            log.error("failed to get the category transaction mapping file", e);
            return Mono.error(e);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(file -> {
            Map<String, String> descriptionCategoryMap = loadMappingFile(file);
            log.debug("description to category map " + descriptionCategoryMap);
            Flux<Category> categories = categoryRepository.findByBudgetId(budgetId);
            Mono<Map<String, Long>> categoryMapMono = categories.collectMap(Category::getName, Category::getCategoryId);
            return categoryMapMono
                .flatMapMany(categoryMap ->
                    transactionRepository.findByBudgetId(budgetId).flatMap(transaction -> {
                        String description = transaction.getDescription();
                        return Flux.fromIterable(descriptionCategoryMap.entrySet())
                            .filter(entry -> description.toLowerCase().contains(entry.getKey().toLowerCase()))
                            .next()
                            .flatMap(entry -> {
                                String mappingCategoryName = entry.getValue();
                                Long categoryId = categoryMap.get(mappingCategoryName);
                                if (categoryId != null) {
                                    log.trace("transaction " + transaction.getTransactionId() + " " + transaction.getDescription()
                                        + " category " + categoryId + " " + mappingCategoryName);
                                    transaction.setCategoryId(categoryId);
                                    return transactionRepository.save(transaction);
                                }
                                return Mono.empty();
                            });
                    })
                )
                .then();
        });
    }

    public Mono<PeriodSummaryDto> summariseCategoryTransactions(
        Long budgetId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByDateBetween(startDate, endDate)
            .filter(transaction -> transaction.getBudgetId().longValue() == budgetId)
            .doOnNext(transaction -> log.debug("found transaction " + transaction))
            .subscribeOn(Schedulers.boundedElastic())
            .collect(Collectors.groupingBy(
                Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add) // This will work with BigDecimal
            ))
            .flatMap(categorySumMap ->
                Flux.fromIterable(categorySumMap.entrySet())
                    .flatMap(entry -> categoryRepository.findById(entry.getKey())
                        .map(category -> new CategorySummaryDto(
                            category.getCategoryId(), category.getName(), entry.getValue())))
                    .collectList()
                    .map(categorySumDtos -> {
                        // Sorting categories by amount in descending order
                        log.debug("categorySumDtos " + categorySumDtos);
                        categorySumDtos.sort((e1, e2) -> e2.getAmount().compareTo(e1.getAmount()));
                        // Creating PeriodSummaryDto and setting the values
                        PeriodSummaryDto periodSummaryDto = new PeriodSummaryDto();
                        periodSummaryDto.setStartDate(startDate);
                        periodSummaryDto.setEndDate(endDate);
                        periodSummaryDto.setCategories(categorySumDtos);
                        log.debug("periodSummaryDto " + periodSummaryDto);
                        return periodSummaryDto;
                    })
            );
    }
}
