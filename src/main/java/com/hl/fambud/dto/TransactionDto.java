package com.hl.fambud.dto;

import com.hl.fambud.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class TransactionDto {

    private Long transactionId;

    private Long transactorId;

    private Long categoryId;

    private Long budgetId;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    @NotNull
    private TransactionType type;
}
