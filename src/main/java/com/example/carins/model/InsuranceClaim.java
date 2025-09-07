package com.example.carins.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "insuranceclaim")
public class InsuranceClaim {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Car car;

    @NotNull
    @Column(nullable = false)
    private LocalDate claimDate;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String description;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public InsuranceClaim() {
        this.createdAt = LocalDateTime.now();
    }

    public InsuranceClaim(Car car, LocalDate claimDate, String description, BigDecimal amount) {
        this();
        this.car = car;
        this.claimDate = claimDate;
        this.description = description;
        this.amount = amount;
    }

}