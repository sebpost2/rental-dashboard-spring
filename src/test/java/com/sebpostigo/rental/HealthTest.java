package com.sebpostigo.rental;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class HealthTest extends IntegrationTest {

	@Test
	void healthCheckReturnsOk() throws Exception {
		mvc.perform(get("/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ok"));
	}

	@Test
	void wrongMethodIsA405ProblemNotA500() throws Exception {
		mvc.perform(post("/health"))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
	}

	@Test
	void protectedEndpointWithoutTokenIs401WithBearerChallenge() throws Exception {
		mvc.perform(get("/properties"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", "Bearer"))
			.andExpect(jsonPath("$.detail").value("Could not validate credentials"));
	}

}
