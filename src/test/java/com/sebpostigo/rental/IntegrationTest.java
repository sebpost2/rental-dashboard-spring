package com.sebpostigo.rental;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sebpostigo.rental.security.RateLimiter;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(properties = "app.jwt.secret=test-secret-that-is-at-least-32-characters")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

	protected static final String PASSWORD = "password123";

	@Autowired
	protected MockMvc mvc;

	@Autowired
	protected JdbcClient jdbc;

	@Autowired
	private RateLimiter rateLimiter;

	@BeforeEach
	void resetState() {
		jdbc.sql("truncate table users restart identity cascade").update();
		rateLimiter.reset();
	}

	protected ResultActions register(String email) throws Exception {
		return mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, PASSWORD)));
	}

	protected ResultActions login(String email, String password) throws Exception {
		return mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", email)
			.param("password", password));
	}

	/** Registers and logs in a user, returning their auth cookie. Uses 1 register + 1 login of the rate budget. */
	protected Cookie signUp(String email) throws Exception {
		register(email).andExpect(status().isCreated());
		return login(email, PASSWORD).andExpect(status().isOk()).andReturn().getResponse().getCookie("access_token");
	}

	protected long createProperty(Cookie owner, String name) throws Exception {
		String body = mvc.perform(post("/properties").cookie(owner)
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"%s\",\"address\":\"Av. Test 123\"}".formatted(name)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

}
