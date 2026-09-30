package com.sebpostigo.rental.property;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PropertyCreateRequest(
		@NotBlank @Size(max = 200) String name,
		@NotBlank @Size(max = 500) String address,
		@Size(max = 50) String propertyType,
		@Size(min = 3, max = 3) String currency) {
}
