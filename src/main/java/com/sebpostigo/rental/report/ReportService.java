package com.sebpostigo.rental.report;

import com.sebpostigo.rental.property.PropertyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReportService {

	private final PropertyService propertyService;

	private final ReportRepository reports;

	public ReportService(PropertyService propertyService, ReportRepository reports) {
		this.propertyService = propertyService;
		this.reports = reports;
	}

	public SummaryResponse summary(long ownerId, Long propertyId, LocalDate start, LocalDate end) {
		ReportFilter filter = filter(ownerId, propertyId, start, end);
		BigDecimal income = reports.totalIncome(filter);
		BigDecimal expenses = reports.totalExpenses(filter);
		return new SummaryResponse(income, expenses, income.subtract(expenses));
	}

	public List<TimeseriesPoint> timeseries(long ownerId, Long propertyId, LocalDate start, LocalDate end) {
		return reports.timeseries(filter(ownerId, propertyId, start, end));
	}

	public String exportCsv(long ownerId, Long propertyId, LocalDate start, LocalDate end) {
		StringBuilder csv = new StringBuilder("type,date,amount,description\r\n");
		for (TransactionRow row : reports.transactions(filter(ownerId, propertyId, start, end))) {
			csv.append(row.type())
				.append(',')
				.append(row.date())
				.append(',')
				.append(row.amount().toPlainString())
				.append(',')
				.append(csvField(row.description()))
				.append("\r\n");
		}
		return csv.toString();
	}

	private ReportFilter filter(long ownerId, Long propertyId, LocalDate start, LocalDate end) {
		if (propertyId != null) {
			propertyService.requireOwned(ownerId, propertyId);
		}
		return new ReportFilter(ownerId, propertyId, start, end);
	}

	/** RFC 4180 quoting, same as Python's csv.writer. */
	static String csvField(String value) {
		if (value == null) {
			return "";
		}
		if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

}
