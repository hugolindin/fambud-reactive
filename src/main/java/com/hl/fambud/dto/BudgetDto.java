package com.hl.fambud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class BudgetDto {

    private Long budgetId;

    @NotBlank
    @Size(max = 200)
    private String name;

    private List<CategoryDto> categories;

    private List<TransactorDto> transactors;
}
