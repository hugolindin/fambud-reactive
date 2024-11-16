package com.hl.fambud.mapper;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.dto.CategoryDto;
import com.hl.fambud.model.Budget;
import com.hl.fambud.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetMapper INSTANCE = Mappers.getMapper(BudgetMapper.class);

    BudgetDto toBudgetDto(Budget budget);

    Budget toBudget(BudgetDto budgetDto);

    void updateBudgetFromDto(BudgetDto budgetDto, @MappingTarget Budget budget);

    CategoryDto categoryToCategoryDto(Category category);

    Category toCategory(CategoryDto categoryDto);

    void updateCategoryFromDto(CategoryDto categoryDto, @MappingTarget Category category);
}
