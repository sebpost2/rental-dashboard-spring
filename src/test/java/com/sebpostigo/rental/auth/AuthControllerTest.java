package com.sebpostigo.rental.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sebpostigo.rental.IntegrationTest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;

class AuthControllerTest extends IntegrationTest {

	@Test
	void registerCreatesUser() throws Exception {
		register("ana@example.com").andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.email").value("ana@example.com"));
	}

	@Test
	void registerDuplicateEmailRejected() throws Exception {
		register("ana@example.com").andExpect(status().isCreated());

		register("ana@example.com").andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Email already registered"));
	}

	@Test
	void registerWithShortPasswordRejected() throws Exception {
		mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"ana@example.com\",\"password\":\"short\"}"))
			.andExpect(status().is(422))
			.andExpect(jsonPath("$.detail").value(containsString("password")));
	}

	@Test
	void loginWithCorrectCredentialsReturnsToken() throws Exception {
		register("ana@example.com");

		login("ana@example.com", PASSWORD).andExpect(status().isOk())
			.andExpect(jsonPath("$.access_token").isNotEmpty())
			.andExpect(jsonPath("$.token_type").value("bearer"));
	}

	@Test
	void loginWithWrongPasswordRejected() throws Exception {
		register("ana@example.com");

		login("ana@example.com", "wrong-password").andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", "Bearer"))
			.andExpect(jsonPath("$.detail").value("Incorrect email or password"));
	}

	@Test
	void loginWithUnknownEmailRejected() throws Exception {
		login("ghost@example.com", PASSWORD).andExpect(status().isUnauthorized());
	}

	@Test
	void meWithoutTokenRejected() throws Exception {
		mvc.perform(get("/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(header().string("WWW-Authenticate", "Bearer"));
	}

	@Test
	void meWithValidBearerTokenReturnsCurrentUser() throws Exception {
		register("ana@example.com");
		String body = login("ana@example.com", PASSWORD).andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(body, "$.access_token");

		mvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("ana@example.com"));
	}

	@Test
	void meWithInvalidTokenRejected() throws Exception {
		mvc.perform(get("/auth/me").header("Authorization", "Bearer not-a-real-token"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Could not validate credentials"));
	}

	@Test
	void loginSetsHttpOnlyCookie() throws Exception {
		register("ana@example.com");

		MockCookie cookie = (MockCookie) login("ana@example.com", PASSWORD).andReturn()
			.getResponse()
			.getCookie("access_token");

		assertThat(cookie).isNotNull();
		assertThat(cookie.isHttpOnly()).isTrue();
		assertThat(cookie.getPath()).isEqualTo("/");
		assertThat(cookie.getMaxAge()).isEqualTo(86400);
		assertThat(cookie.getSameSite()).isEqualTo("Lax");
		assertThat(cookie.getSecure()).isFalse();
	}

	@Test
	void meWorksViaCookieWithoutHeader() throws Exception {
		Cookie cookie = signUp("ana@example.com");

		mvc.perform(get("/auth/me").cookie(cookie))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("ana@example.com"));
	}

	@Test
	void logoutClearsCookie() throws Exception {
		MockCookie cleared = (MockCookie) mvc.perform(post("/auth/logout"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("ok"))
			.andReturn()
			.getResponse()
			.getCookie("access_token");

		assertThat(cleared).isNotNull();
		assertThat(cleared.getMaxAge()).isZero();
		assertThat(cleared.getValue()).isEmpty();
	}

	@Test
	void loginIsRateLimited() throws Exception {
		for (int i = 0; i < 10; i++) {
			login("ghost@example.com", "wrong").andExpect(status().isUnauthorized());
		}

		login("ghost@example.com", "wrong").andExpect(status().isTooManyRequests())
			.andExpect(jsonPath("$.detail").value("Too many requests"));
	}

	@Test
	void registerIsRateLimited() throws Exception {
		for (int i = 0; i < 10; i++) {
			register("user" + i + "@example.com").andExpect(status().isCreated());
		}

		register("user10@example.com").andExpect(status().isTooManyRequests());
	}

	@Test
	void loginFromForeignOriginRejected() throws Exception {
		mvc.perform(post("/auth/login").header("Origin", "https://evil.example")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "ana@example.com")
			.param("password", PASSWORD))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.detail").value("Invalid origin"));
	}

	@Test
	void loginFromFrontendOriginAllowed() throws Exception {
		register("ana@example.com");

		mvc.perform(post("/auth/login").header("Origin", "http://localhost:3000")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "ana@example.com")
			.param("password", PASSWORD))
			.andExpect(status().isOk());
	}

	@Test
	void staleCookieDoesNotBlockLogin() throws Exception {
		register("ana@example.com");

		mvc.perform(post("/auth/login").cookie(new Cookie("access_token", "expired-or-garbage"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("username", "ana@example.com")
			.param("password", PASSWORD))
			.andExpect(status().isOk());
	}

	@Test
	void corsPreflightFromFrontendAllowsCredentials() throws Exception {
		mvc.perform(options("/auth/login").header("Origin", "http://localhost:3000")
			.header("Access-Control-Request-Method", "POST"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
			.andExpect(header().string("Access-Control-Allow-Credentials", "true"));
	}

}
