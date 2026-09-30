package com.sebpostigo.rental.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sebpostigo.rental.common.AppProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtServiceTest {

	private static final String SECRET = "unit-test-secret-that-is-at-least-32-chars";

	private final JwtDecoder decoder = SecurityConfig.jwtDecoderFor(SecurityConfig.signingKey(SECRET));

	private static JwtService serviceSigningWith(String secret, Instant now) {
		SecretKey key = SecurityConfig.signingKey(secret);
		AppProperties props = new AppProperties("development", List.of("http://localhost:3000"),
				new AppProperties.Jwt(secret, Duration.ofHours(24)));
		return new JwtService(SecurityConfig.jwtEncoderFor(key), props, Clock.fixed(now, ZoneOffset.UTC));
	}

	@Test
	void tokenRoundTripsEmailAndUserId() {
		String token = serviceSigningWith(SECRET, Instant.now()).issue(42L, "ana@example.com");

		Jwt jwt = decoder.decode(token);

		assertThat(jwt.getSubject()).isEqualTo("ana@example.com");
		assertThat(CurrentUser.id(jwt)).isEqualTo(42L);
	}

	@Test
	void expiredTokenIsRejected() {
		String token = serviceSigningWith(SECRET, Instant.now().minus(Duration.ofDays(2))).issue(1L, "ana@example.com");

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void tokenSignedWithAnotherKeyIsRejected() {
		String token = serviceSigningWith("a-different-secret-that-is-also-32-chars", Instant.now())
			.issue(1L, "ana@example.com");

		assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
	}

}
