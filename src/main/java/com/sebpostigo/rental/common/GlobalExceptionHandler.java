package com.sebpostigo.rental.common;

import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// 422 is what FastAPI returns for validation errors; kept so the frontend and tests match.
	private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ProblemDetail> handleApi(ApiException ex) {
		ResponseEntity.BodyBuilder response = ResponseEntity.status(ex.status());
		if (ex.status() == HttpStatus.UNAUTHORIZED) {
			response.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		}
		return response.body(ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unhandled exception", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detail = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> snakeCase(error.getField()) + ": " + error.getDefaultMessage())
			.sorted()
			.collect(Collectors.joining("; "));
		return unprocessable(detail);
	}

	@Override
	protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String detail = ex.getAllErrors()
			.stream()
			.map(MessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.joining("; "));
		return unprocessable(detail);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
		return unprocessable(cause instanceof IllegalArgumentException ? cause.getMessage() : "Malformed request body");
	}

	@Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatusCode status, WebRequest request) {
		return unprocessable(ex.getPropertyName() + ": invalid value");
	}

	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		return unprocessable(ex.getParameterName() + ": is required");
	}

	private static ResponseEntity<Object> unprocessable(String detail) {
		return ResponseEntity.status(UNPROCESSABLE).body(ProblemDetail.forStatusAndDetail(UNPROCESSABLE, detail));
	}

	static String snakeCase(String field) {
		return field.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
	}

}
