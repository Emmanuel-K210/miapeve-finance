package com.miapeve.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TransactionRequest(
        @NotBlank @Pattern(regexp = "CREDIT|DEBIT", message = "doit être CREDIT ou DEBIT") String typeTransaction,
        @DecimalMin(value = "0.01", message = "doit être supérieur à 0") double montant,
        @NotNull Long compteId,
        String description,
        String reference
) {
}
