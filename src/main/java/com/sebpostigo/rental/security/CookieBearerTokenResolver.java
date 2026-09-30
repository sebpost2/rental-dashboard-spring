package com.sebpostigo.rental.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.util.WebUtils;

/**
 * Reads the JWT from the access_token cookie, falling back to the Authorization header. Public
 * endpoints get no token at all, so a stale cookie can never block login or registration.
 */
class CookieBearerTokenResolver implements BearerTokenResolver {

	static final String COOKIE_NAME = "access_token";

	private final DefaultBearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

	@Override
	public String resolve(HttpServletRequest request) {
		if (isPublic(request.getRequestURI())) {
			return null;
		}
		Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
		if (cookie != null && !cookie.getValue().isBlank()) {
			return cookie.getValue();
		}
		return headerResolver.resolve(request);
	}

	static boolean isPublic(String path) {
		for (String pattern : SecurityConfig.PUBLIC_PATHS) {
			boolean matches = pattern.endsWith("/**") ? path.startsWith(pattern.substring(0, pattern.length() - 3))
					: path.equals(pattern);
			if (matches) {
				return true;
			}
		}
		return false;
	}

}
