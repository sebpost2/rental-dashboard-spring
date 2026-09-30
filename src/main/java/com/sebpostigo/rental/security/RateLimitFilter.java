package com.sebpostigo.rental.security;

import com.sebpostigo.rental.common.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/** 10 requests/minute per client IP and endpoint on login and register. */
class RateLimitFilter extends OncePerRequestFilter {

	private static final Set<String> LIMITED_PATHS = Set.of("/auth/login", "/auth/register");

	private final RateLimiter rateLimiter;

	RateLimitFilter(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !("POST".equals(request.getMethod()) && LIMITED_PATHS.contains(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		// remoteAddr is the real client IP behind Render's proxy thanks to server.forward-headers-strategy.
		String key = request.getRemoteAddr() + " " + request.getRequestURI();
		if (!rateLimiter.tryConsume(key)) {
			ProblemResponses.write(response, HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
			return;
		}
		chain.doFilter(request, response);
	}

}
