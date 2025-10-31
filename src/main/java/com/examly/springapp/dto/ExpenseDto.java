package com.examly.springapp.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseDto {
    private Long id;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String description;

    @NotNull
    private LocalDate date;

    private String paymentMethod;
    private String receiptUrl;
    private Boolean isRecurring = false;

    @NotBlank
    private String categoryName;
}