package com.hl.fambud.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transactors")
public class Transactor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transactor_sequence")
    @SequenceGenerator(name = "transactor_sequence", sequenceName = "transactor_sequence", allocationSize = 1)
    private Long transactorId;

    private Long budgetId;

    private String firstName;

    private String lastName;

    private String email;
}
