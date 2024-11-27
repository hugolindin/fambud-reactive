package com.hl.fambud.service;

import com.hl.fambud.model.Category;
import com.hl.fambud.model.Transaction;
import com.hl.fambud.repository.CategoryRepository;
import com.hl.fambud.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCategoriser {

    private final CategoryRepository categoryRepository;

    private final TransactionRepository transactionRepository;

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

    public Mono<Void> categorise(Long budgetId, List<Transaction> transactionList, File mappingFile) throws IOException {
        log.debug("budget " + budgetId + " transactions " + transactionList.size());
        Flux<Category> categories = categoryRepository.findByBudgetId(budgetId);
        Mono<Map<String, Long>> categoryMapMono = categories.collectMap(Category::getName, Category::getCategoryId);
        Map<String, String> descriptionCategoryMap = loadMappingFile(mappingFile);
        log.debug("mapping file map " + descriptionCategoryMap);
        Flux<Transaction> transactionFlux = Flux.fromIterable(transactionList);
        return categoryMapMono
            .flatMapMany(categoryMap ->
                transactionFlux.flatMap(transaction -> {
                    String description = transaction.getDescription();
                    return Flux.fromIterable(descriptionCategoryMap.entrySet())
                        .filter(entry -> description.toLowerCase().contains(entry.getKey().toLowerCase()))
                        .next()
                        .map(entry -> {
                            String mappingCategoryName = entry.getValue();
                            Long categoryId = categoryMap.get(mappingCategoryName);
                            if (categoryId != null) {
                                log.trace("transaction " + transaction.getTransactionId() + " " + transaction.getDescription()
                                    + " category " + categoryId + " " + mappingCategoryName);
                                transaction.setCategoryId(categoryId);
                                transactionRepository.save(transaction);
                            }
                            return transaction;
                        });
                })
            )
            .then();
    }
}
