package com.hl.fambud.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PeriodSummaryDto {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<CategorySummaryDto> categories;
}
