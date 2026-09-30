package com.sebpostigo.rental.ledger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum ExpenseCategory {

	CLEANING, MAINTENANCE, COMMISSION, OTHER;

	/** Lowercase on the wire and in the database, as in the FastAPI version. */
	@JsonValue
	public String value() {
		return name().toLowerCase(Locale.ROOT);
	}

	@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
	public static ExpenseCategory fromValue(String value) {
		for (ExpenseCategory category : values()) {
			if (category.value().equals(value)) {
				return category;
			}
		}
		throw new IllegalArgumentException("category: must be one of cleaning, maintenance, commission, other");
	}

}
