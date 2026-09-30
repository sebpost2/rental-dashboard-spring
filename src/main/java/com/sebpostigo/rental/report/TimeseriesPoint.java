package com.sebpostigo.rental.report;

import java.math.BigDecimal;

public record TimeseriesPoint(String period, BigDecimal income, BigDecimal expenses) {
}
