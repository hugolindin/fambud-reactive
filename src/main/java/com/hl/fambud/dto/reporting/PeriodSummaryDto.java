package com.hl.fambud.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeriodSummaryDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private BigDecimal surplus;
    private List<CategorySummaryDto> expenseCategories;
    private List<CategorySummaryDto> incomeCategories;
}
