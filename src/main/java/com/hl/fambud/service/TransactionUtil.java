package com.hl.fambud.service;

import com.hl.fambud.model.Transaction;
import com.hl.fambud.model.TransactionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class TransactionUtil {

    static Mono<MultipartFile> convertToMultipartFile(Scheduler parallelScheduler, FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
            .map(dataBuffer -> new MultipartFile() {
                @Override
                public String getName() {
                    return filePart.filename();
                }

                @Override
                public String getOriginalFilename() {
                    return filePart.filename();
                }

                @Override
                public String getContentType() {
                    return filePart.headers().getContentType().toString();
                }

                @Override
                public boolean isEmpty() {
                    return dataBuffer.readableByteCount() == 0;
                }

                @Override
                public long getSize() {
                    return dataBuffer.readableByteCount();
                }

                @Override
                public byte[] getBytes() throws IOException {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return dataBuffer.asInputStream();
                }

                @Override
                public void transferTo(File dest) throws IOException, IllegalStateException {
                    try (FileOutputStream outputStream = new FileOutputStream(dest)) {
                        outputStream.write(getBytes());
                    }
                }
            });
    }

    static Flux<Transaction> parseCsvFile(Scheduler parallelScheduler, Long budgetId, MultipartFile file) {
        return Mono.fromCallable(() -> file.getInputStream())
            .subscribeOn(parallelScheduler)
            .flatMapMany(inputStream -> {
                try (Reader reader = new InputStreamReader(inputStream);
                     CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

                    List<CSVRecord> records = csvParser.getRecords();

                    // Process records in parallel using Flux from a list
                    return Flux.fromIterable(records)
                        .parallel()
                        .runOn(parallelScheduler)
                        .flatMap(record -> mapToTransaction(budgetId, record))
                        .sequential();  // Gather results back into a single sequence

                } catch (IOException e) {
                    log.error("Error reading CSV file", e);
                    return Flux.error(e);
                }
            });
    }

    static Mono<Transaction> mapToTransaction(Long budgetId, CSVRecord csvRecord) {
        try {
            Transaction transaction = new Transaction();
            transaction.setBudgetId(budgetId);
            transaction.setDescription(csvRecord.get("Description"));

            String dateStr = csvRecord.get("Date");
            transaction.setDate(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            String debitAmount = csvRecord.get("Debit Amount");
            String creditAmount = csvRecord.get("Credit Amount");

            if (!debitAmount.isEmpty()) {
                transaction.setAmount(new BigDecimal(debitAmount));
                transaction.setType(TransactionType.EXPENSE);
            } else if (!creditAmount.isEmpty()) {
                transaction.setAmount(new BigDecimal(creditAmount));
                transaction.setType(TransactionType.INCOME);
            }

            return Mono.just(transaction);
        } catch (Exception e) {
            log.error("Error mapping CSVRecord to Transaction", e);
            return Mono.error(e);
        }
    }
}
