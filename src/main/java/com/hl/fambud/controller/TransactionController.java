package com.hl.fambud.controller;

import com.hl.fambud.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@AllArgsConstructor
@RequestMapping("${app.base-url}/transactions")
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping(value = "/import/{transactorId}")
    public Mono<ResponseEntity<String>> startCsvImport(
        @PathVariable Long transactorId, @RequestPart("file") FilePart filePart) {
        log.debug("file import for transactorId " + transactorId);
        return transactionService.startCsvImport(transactorId, filePart)
            .map(importJobId -> new ResponseEntity<>(importJobId, HttpStatus.ACCEPTED))
            .onErrorResume(e -> Mono.just(new ResponseEntity<>("Failed to initiate import", HttpStatus.BAD_REQUEST)));
    }

}
