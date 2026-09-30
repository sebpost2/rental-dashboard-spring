package com.sebpostigo.rental.ledger;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeResponse(long id, LocalDate date, BigDecimal amount, String source, String notes) {

	static IncomeResponse from(Income income) {
		return new IncomeResponse(income.getId(), income.getDate(), income.getAmount(), income.getSource(),
				income.getNotes());
	}

}
