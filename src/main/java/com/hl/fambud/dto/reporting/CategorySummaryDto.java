package com.hl.fambud.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CategorySummaryDto {

    private String categoryName;
    private BigDecimal amount;
}
