package com.hl.fambud.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "budget_sequence")
    @SequenceGenerator(name = "budget_sequence", sequenceName = "budget_sequence", allocationSize = 1)
    private Long budgetId;

    private String name;

    @Transient
    private List<Category> categories;

    @Transient
    private List<Transactor> transactors;

}
