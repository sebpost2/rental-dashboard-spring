package com.sebpostigo.rental.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.sebpostigo.rental.IntegrationTest;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/** Needs a real Tomcat: forwarded headers are resolved by the server, which MockMvc bypasses. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "app.jwt.secret=test-secret-that-is-at-least-32-characters")
class ForwardedClientIpTest extends IntegrationTest {

	private final HttpClient http = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	private int loginVia(String forwardedFor) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/login"))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.header("X-Forwarded-For", forwardedFor)
			.POST(HttpRequest.BodyPublishers.ofString("username=ghost%40example.com&password=wrong"))
			.build();
		return http.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
	}

	/** Render's proxy appends the real client IP; anything the client put before it is untrusted. */
	@Test
	void spoofedForwardedForDoesNotEscapeTheRateLimit() throws Exception {
		for (int i = 0; i < 10; i++) {
			assertThat(loginVia("1.2.3." + i + ", 203.0.113.7")).isEqualTo(401);
		}

		assertThat(loginVia("1.2.3.99, 203.0.113.7")).isEqualTo(429);
	}

}
