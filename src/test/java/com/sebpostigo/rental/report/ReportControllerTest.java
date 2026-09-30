package com.sebpostigo.rental.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sebpostigo.rental.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportControllerTest extends IntegrationTest {

	private Cookie ana;

	private long casa;

	private long depa;

	@BeforeEach
	void seedLedger() throws Exception {
		ana = signUp("ana@example.com");
		casa = createProperty(ana, "Casa Sol");
		depa = createProperty(ana, "Depa Luna");
		addIncome(ana, casa, "2026-06-01", "1000.00", "Rent");
		addIncome(ana, casa, "2026-06-15", "200.00", "Parking");
		addIncome(ana, depa, "2026-07-01", "500.00", "Rent");
		addExpense(ana, casa, "2026-07-10", "100.00", "maintenance");
	}

	@Test
	void summaryReturnsTotalsAndNetBalance() throws Exception {
		mvc.perform(get("/reports/summary").cookie(ana))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total_income").value(1700.0))
			.andExpect(jsonPath("$.total_expenses").value(100.0))
			.andExpect(jsonPath("$.net_balance").value(1600.0));
	}

	@Test
	void summaryFiltersByDateRange() throws Exception {
		mvc.perform(get("/reports/summary").cookie(ana).param("start_date", "2026-06-10").param("end_date", "2026-06-30"))
			.andExpect(jsonPath("$.total_income").value(200.0))
			.andExpect(jsonPath("$.total_expenses").value(0.0));
	}

	@Test
	void summaryFiltersByProperty() throws Exception {
		mvc.perform(get("/reports/summary").cookie(ana).param("property_id", String.valueOf(depa)))
			.andExpect(jsonPath("$.total_income").value(500.0))
			.andExpect(jsonPath("$.total_expenses").value(0.0));
	}

	@Test
	void summaryExcludesOtherUsersData() throws Exception {
		Cookie ben = signUp("ben@example.com");
		long benHouse = createProperty(ben, "Ben House");
		addIncome(ben, benHouse, "2026-06-01", "9999.00", "Rent");

		mvc.perform(get("/reports/summary").cookie(ana)).andExpect(jsonPath("$.total_income").value(1700.0));
	}

	@Test
	void summaryWithNoDataReturnsZeros() throws Exception {
		Cookie ben = signUp("ben@example.com");

		mvc.perform(get("/reports/summary").cookie(ben))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total_income").value(0.0))
			.andExpect(jsonPath("$.total_expenses").value(0.0))
			.andExpect(jsonPath("$.net_balance").value(0.0));
	}

	@Test
	void summaryRejectsOtherUsersPropertyFilter() throws Exception {
		Cookie ben = signUp("ben@example.com");

		mvc.perform(get("/reports/summary").cookie(ben).param("property_id", String.valueOf(casa)))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Property not found"));
	}

	@Test
	void timeseriesGroupsByMonth() throws Exception {
		mvc.perform(get("/reports/timeseries").cookie(ana))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].period").value("2026-06"))
			.andExpect(jsonPath("$[0].income").value(1200.0))
			.andExpect(jsonPath("$[0].expenses").value(0.0))
			.andExpect(jsonPath("$[1].period").value("2026-07"))
			.andExpect(jsonPath("$[1].income").value(500.0))
			.andExpect(jsonPath("$[1].expenses").value(100.0));
	}

	@Test
	void exportCsvReturnsCombinedTransactionsSortedByDate() throws Exception {
		String csv = mvc.perform(get("/reports/export").cookie(ana))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/csv"))
			.andExpect(header().string("Content-Disposition", "attachment; filename=transactions.csv"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(csv.split("\r\n")).containsExactly("type,date,amount,description",
				"income,2026-06-01,1000.00,Rent", "income,2026-06-15,200.00,Parking",
				"income,2026-07-01,500.00,Rent", "expense,2026-07-10,100.00,maintenance");
	}

	@Test
	void exportCsvQuotesDescriptionsContainingCommas() throws Exception {
		addIncome(ana, casa, "2026-08-01", "10.00", "Rent, June");

		mvc.perform(get("/reports/export").cookie(ana))
			.andExpect(content().string(containsString("income,2026-08-01,10.00,\"Rent, June\"\r\n")));
	}

	@Test
	void malformedQueryParametersAre422() throws Exception {
		mvc.perform(get("/reports/summary").cookie(ana).param("start_date", "junk"))
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.detail").value("start_date: invalid value"));
		mvc.perform(get("/reports/summary").cookie(ana).param("property_id", "abc"))
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.detail").value("property_id: invalid value"));
	}

}
