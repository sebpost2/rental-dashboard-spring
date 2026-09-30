package com.sebpostigo.rental.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;

public record RegisterRequest(@NotBlank @Email String email, @NotNull @Size(min = 8, max = 72) String password) {

	/** @Size counts chars, but bcrypt rejects anything past 72 bytes (e.g. 40 × "ñ"). */
	@JsonIgnore
	@AssertTrue(message = "must be at most 72 bytes")
	public boolean isPasswordWithinBcryptLimit() {
		return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
	}

}
