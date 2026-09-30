package com.sebpostigo.rental.report;

import java.math.BigDecimal;
import java.time.LocalDate;

record TransactionRow(String type, LocalDate date, BigDecimal amount, String description) {
}
