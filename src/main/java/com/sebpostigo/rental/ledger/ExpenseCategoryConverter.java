package com.sebpostigo.rental.ledger;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ExpenseCategoryConverter implements AttributeConverter<ExpenseCategory, String> {

	@Override
	public String convertToDatabaseColumn(ExpenseCategory category) {
		return category == null ? null : category.value();
	}

	@Override
	public ExpenseCategory convertToEntityAttribute(String value) {
		return value == null ? null : ExpenseCategory.fromValue(value);
	}

}
