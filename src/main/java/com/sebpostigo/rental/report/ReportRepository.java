package com.sebpostigo.rental.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Read-side SQL: reports aggregate in the database instead of loading every row. */
@Repository
class ReportRepository {

	private final JdbcClient jdbc;

	ReportRepository(JdbcClient jdbc) {
		this.jdbc = jdbc;
	}

	BigDecimal totalIncome(ReportFilter filter) {
		return total("incomes", filter);
	}

	BigDecimal totalExpenses(ReportFilter filter) {
		return total("expenses", filter);
	}

	// table is always one of the two constants above, never user input.
	private BigDecimal total(String table, ReportFilter filter) {
		String sql = "select coalesce(sum(t.amount), 0) from " + table
				+ " t join properties p on p.id = t.property_id where " + filter.where("t");
		return jdbc.sql(sql).params(filter.params()).query(BigDecimal.class).single();
	}

	List<TimeseriesPoint> timeseries(ReportFilter filter) {
		String sql = """
				select period, sum(income) as income, sum(expenses) as expenses
				from (
				    select to_char(t.date, 'YYYY-MM') as period, t.amount as income, 0::numeric as expenses
				    from incomes t join properties p on p.id = t.property_id
				    where %1$s
				    union all
				    select to_char(t.date, 'YYYY-MM'), 0::numeric, t.amount
				    from expenses t join properties p on p.id = t.property_id
				    where %1$s
				) months
				group by period
				order by period
				""".formatted(filter.where("t"));
		return jdbc.sql(sql)
			.params(filter.params())
			.query((rs, rowNum) -> new TimeseriesPoint(rs.getString("period"), rs.getBigDecimal("income"),
					rs.getBigDecimal("expenses")))
			.list();
	}

	List<TransactionRow> transactions(ReportFilter filter) {
		String sql = """
				select 'income' as type, t.date, t.amount, t.source as description, 0 as kind
				from incomes t join properties p on p.id = t.property_id
				where %1$s
				union all
				select 'expense', t.date, t.amount, t.category, 1
				from expenses t join properties p on p.id = t.property_id
				where %1$s
				order by date, kind
				""".formatted(filter.where("t"));
		return jdbc.sql(sql)
			.params(filter.params())
			.query((rs, rowNum) -> new TransactionRow(rs.getString("type"), rs.getObject("date", LocalDate.class),
					rs.getBigDecimal("amount"), rs.getString("description")))
			.list();
	}

}
