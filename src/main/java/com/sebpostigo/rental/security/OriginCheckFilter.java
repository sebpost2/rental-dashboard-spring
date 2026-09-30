package com.sebpostigo.rental.security;

import com.sebpostigo.rental.common.ProblemResponses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CSRF guard for login/logout: browsers can send these form POSTs cross-site without a CORS
 * preflight, so a present Origin header must be one of ours. Requests without Origin (curl,
 * server-to-server) are allowed, as in the FastAPI version.
 */
class OriginCheckFilter extends OncePerRequestFilter {

	private static final Set<String> GUARDED_PATHS = Set.of("/auth/login", "/auth/logout");

	private final Set<String> allowedOrigins;

	OriginCheckFilter(List<String> allowedOrigins) {
		this.allowedOrigins = Set.copyOf(allowedOrigins);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !("POST".equals(request.getMethod()) && GUARDED_PATHS.contains(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String origin = request.getHeader("Origin");
		if (origin != null && !allowedOrigins.contains(origin)) {
			ProblemResponses.write(response, HttpStatus.FORBIDDEN, "Invalid origin");
			return;
		}
		chain.doFilter(request, response);
	}

}
