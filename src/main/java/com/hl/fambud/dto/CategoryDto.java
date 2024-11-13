package com.hl.fambud.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CategoryDto {

    private Long categoryId;

    private Long budgetId;

    @NotBlank
    @Size(max = 200)
    private String name;
}
