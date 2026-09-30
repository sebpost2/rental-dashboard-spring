package com.sebpostigo.rental.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sebpostigo.rental.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LedgerControllerTest extends IntegrationTest {

	private static String incomeJson(String date, String amount, String source) {
		return "{\"date\":\"%s\",\"amount\":%s,\"source\":\"%s\"}".formatted(date, amount, source);
	}

	@Test
	void createIncomeRequiresAuth() throws Exception {
		mvc.perform(post("/properties/{id}/incomes", 1).contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "1200", "Rent")))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void createIncomeReturnsCreatedIncome() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/incomes", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"date\":\"2026-06-01\",\"amount\":1200.5,\"source\":\"Rent\",\"notes\":\"June\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.date").value("2026-06-01"))
			.andExpect(jsonPath("$.amount").value(1200.5))
			.andExpect(jsonPath("$.source").value("Rent"))
			.andExpect(jsonPath("$.notes").value("June"));
	}

	@Test
	void createIncomeOnOtherUsersPropertyReturns404() throws Exception {
		Cookie ana = signUp("ana@example.com");
		Cookie ben = signUp("ben@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/incomes", casa).cookie(ben)
			.contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "1200", "Rent")))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Property not found"));
	}

	@Test
	void listIncomeFiltersByDateRangeInclusiveAndOrdersByDate() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");
		addIncome(ana, casa, "2026-08-01", "1200", "Rent");
		addIncome(ana, casa, "2026-06-01", "1200", "Rent");
		addIncome(ana, casa, "2026-07-01", "1200", "Rent");

		mvc.perform(get("/properties/{id}/incomes", casa).cookie(ana)
			.param("start_date", "2026-07-01")
			.param("end_date", "2026-08-01"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].date").value("2026-07-01"))
			.andExpect(jsonPath("$[1].date").value("2026-08-01"));
	}

	@Test
	void deleteIncome() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");
		String body = mvc.perform(post("/properties/{id}/incomes", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "1200", "Rent")))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long incomeId = ((Number) JsonPath.read(body, "$.id")).longValue();

		mvc.perform(delete("/properties/{id}/incomes/{incomeId}", casa, incomeId).cookie(ana))
			.andExpect(status().isNoContent());

		mvc.perform(get("/properties/{id}/incomes", casa).cookie(ana)).andExpect(jsonPath("$.length()").value(0));
		mvc.perform(delete("/properties/{id}/incomes/{incomeId}", casa, incomeId).cookie(ana))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Income not found"));
	}

	@Test
	void createExpenseReturnsCreatedExpense() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/expenses", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"date\":\"2026-06-05\",\"amount\":80,\"category\":\"maintenance\"}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.category").value("maintenance"))
			.andExpect(jsonPath("$.amount").value(80.0));
	}

	@Test
	void createExpenseRejectsInvalidCategory() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/expenses", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"date\":\"2026-06-05\",\"amount\":80,\"category\":\"Utilities\"}"))
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.detail").value(containsString("category")));
	}

	@Test
	void listExpensesFiltersByDateRange() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");
		addExpense(ana, casa, "2026-06-05", "80", "other");
		addExpense(ana, casa, "2026-07-05", "95", "other");

		mvc.perform(get("/properties/{id}/expenses", casa).cookie(ana).param("start_date", "2026-07-01"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].date").value("2026-07-05"));
	}

	@Test
	void expenseOnOtherUsersPropertyReturns404() throws Exception {
		Cookie ana = signUp("ana@example.com");
		Cookie ben = signUp("ben@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(get("/properties/{id}/expenses", casa).cookie(ben)).andExpect(status().isNotFound());
	}

	@Test
	void deleteExpense() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");
		String body = mvc.perform(post("/properties/{id}/expenses", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"date\":\"2026-06-05\",\"amount\":80,\"category\":\"other\"}"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long expenseId = ((Number) JsonPath.read(body, "$.id")).longValue();

		mvc.perform(delete("/properties/{id}/expenses/{expenseId}", casa, expenseId).cookie(ana))
			.andExpect(status().isNoContent());

		mvc.perform(get("/properties/{id}/expenses", casa).cookie(ana)).andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void deletingAPropertyDeletesItsLedgerEntries() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");
		addIncome(ana, casa, "2026-06-01", "1200", "Rent");
		addExpense(ana, casa, "2026-06-05", "80", "other");

		mvc.perform(delete("/properties/{id}", casa).cookie(ana)).andExpect(status().isNoContent());

		long remaining = jdbc.sql("select (select count(*) from incomes) + (select count(*) from expenses)")
			.query(Long.class)
			.single();
		assertThat(remaining).isZero();
	}

	@Test
	void validationErrorsAre422WithAReadableDetail() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/incomes", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "-5", "")))
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.detail").value(allOf(containsString("amount:"), containsString("source:"))));
	}

	@Test
	void amountsThatDoNotFitNumeric10Point2AreRejected() throws Exception {
		Cookie ana = signUp("ana@example.com");
		long casa = createProperty(ana, "Casa Sol");

		mvc.perform(post("/properties/{id}/incomes", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "12.345", "Rent")))
			.andExpect(status().is(422));
		mvc.perform(post("/properties/{id}/incomes", casa).cookie(ana)
			.contentType(MediaType.APPLICATION_JSON)
			.content(incomeJson("2026-06-01", "100000000", "Rent")))
			.andExpect(status().is(422));
	}

}
