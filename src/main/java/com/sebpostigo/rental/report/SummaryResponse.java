package com.sebpostigo.rental.report;

import java.math.BigDecimal;

public record SummaryResponse(BigDecimal totalIncome, BigDecimal totalExpenses, BigDecimal netBalance) {
}
