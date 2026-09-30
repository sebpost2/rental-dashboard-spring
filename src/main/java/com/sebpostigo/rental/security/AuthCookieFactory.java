package com.sebpostigo.rental.security;

import com.sebpostigo.rental.common.AppProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieFactory {

	private final AppProperties props;

	public AuthCookieFactory(AppProperties props) {
		this.props = props;
	}

	public ResponseCookie issue(String token) {
		return cookie(token, props.jwt().expiry());
	}

	public ResponseCookie clear() {
		return cookie("", Duration.ZERO);
	}

	/** Cross-site in production (Vercel frontend → Render API), so SameSite=None requires Secure. */
	private ResponseCookie cookie(String value, Duration maxAge) {
		return ResponseCookie.from(CookieBearerTokenResolver.COOKIE_NAME, value)
			.httpOnly(true)
			.path("/")
			.maxAge(maxAge)
			.secure(props.isProduction())
			.sameSite(props.isProduction() ? "None" : "Lax")
			.build();
	}

}
