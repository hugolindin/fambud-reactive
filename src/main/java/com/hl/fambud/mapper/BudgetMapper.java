package com.hl.fambud.mapper;

import com.hl.fambud.dto.BudgetDto;
import com.hl.fambud.model.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    BudgetMapper INSTANCE = Mappers.getMapper(BudgetMapper.class);

    BudgetDto toBudgetDto(Budget budget);

    Budget toBudget(BudgetDto budgetDto);

    void updateBudgetFromDto(BudgetDto budgetDto, @MappingTarget Budget budget);
}
