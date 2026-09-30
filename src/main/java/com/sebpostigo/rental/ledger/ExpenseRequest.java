package com.sebpostigo.rental.ledger;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
		@NotNull LocalDate date,
		@NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal amount,
		@NotNull ExpenseCategory category,
		@Size(max = 1000) String notes) {
}
