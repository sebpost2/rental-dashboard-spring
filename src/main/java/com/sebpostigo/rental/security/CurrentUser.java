package com.sebpostigo.rental.security;

import org.springframework.security.oauth2.jwt.Jwt;

public final class CurrentUser {

	private CurrentUser() {
	}

	public static long id(Jwt jwt) {
		Number userId = jwt.getClaim(JwtService.USER_ID_CLAIM);
		return userId.longValue();
	}

}
