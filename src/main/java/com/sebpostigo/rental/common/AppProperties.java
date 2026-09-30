package com.sebpostigo.rental.common;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record AppProperties(@NotBlank String environment, @NotEmpty List<String> corsOrigins, @Valid @NotNull Jwt jwt) {

	public record Jwt(@NotBlank @Size(min = 32, message = "must be at least 32 characters") String secret,
			@NotNull Duration expiry) {
	}

	public boolean isProduction() {
		return "production".equals(environment);
	}

}
