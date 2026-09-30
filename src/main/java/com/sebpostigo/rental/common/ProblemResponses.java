package com.sebpostigo.rental.common;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** Writes a ProblemDetail body from servlet filters, which run before Spring MVC's exception handling. */
public final class ProblemResponses {

	private ProblemResponses() {
	}

	public static void write(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}"
			.formatted(status.getReasonPhrase(), status.value(), escape(detail)));
	}

	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

}
