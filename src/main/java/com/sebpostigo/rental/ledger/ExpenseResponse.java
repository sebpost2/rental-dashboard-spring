package com.sebpostigo.rental.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(long id, LocalDate date, BigDecimal amount, ExpenseCategory category, String notes) {

	static ExpenseResponse from(Expense expense) {
		return new ExpenseResponse(expense.getId(), expense.getDate(), expense.getAmount(), expense.getCategory(),
				expense.getNotes());
	}

}
