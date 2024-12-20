package com.hl.fambud.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TransactionSummaryDto {

    private List<PeriodSummaryDto> periods;
}
