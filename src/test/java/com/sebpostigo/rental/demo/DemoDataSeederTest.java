package com.sebpostigo.rental.demo;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sebpostigo.rental.IntegrationTest;
import com.sebpostigo.rental.auth.UserRepository;
import com.sebpostigo.rental.ledger.ExpenseRepository;
import com.sebpostigo.rental.ledger.IncomeRepository;
import com.sebpostigo.rental.property.PropertyRepository;
import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

class DemoDataSeederTest extends IntegrationTest {

	@Autowired
	private UserRepository users;

	@Autowired
	private PropertyRepository properties;

	@Autowired
	private IncomeRepository incomes;

	@Autowired
	private ExpenseRepository expenses;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private DemoDataSeeder seederOn(String isoDate) {
		Clock clock = Clock.fixed(Instant.parse(isoDate + "T12:00:00Z"), ZoneOffset.UTC);
		return new DemoDataSeeder(users, properties, incomes, expenses, passwordEncoder, clock);
	}

	@Test
	void seedingTwiceLeavesOneFreshDemoDatasetForTheLastThreeMonths() throws Exception {
		seederOn("2026-09-15").seed();
		seederOn("2026-09-15").seed();

		Cookie demo = login(DemoDataSeeder.EMAIL, DemoDataSeeder.PASSWORD).andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getCookie("access_token");

		String propertiesJson = mvc.perform(get("/properties").cookie(demo))
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].name").value("Casa Miraflores 402"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		long propertyId = ((Number) JsonPath.read(propertiesJson, "$[0].id")).longValue();

		mvc.perform(get("/properties/{id}/expenses", propertyId).cookie(demo))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(4));
		mvc.perform(get("/reports/timeseries").cookie(demo))
			.andExpect(jsonPath("$[*].period", contains("2026-06", "2026-07", "2026-08")));
		mvc.perform(get("/reports/summary").cookie(demo))
			.andExpect(jsonPath("$.total_income").value(3600.0))
			.andExpect(jsonPath("$.total_expenses").value(480.0));
	}

}
