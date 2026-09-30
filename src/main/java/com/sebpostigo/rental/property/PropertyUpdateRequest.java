package com.sebpostigo.rental.property;

import jakarta.validation.constraints.Size;

public record PropertyUpdateRequest(
		@Size(min = 1, max = 200) String name,
		@Size(min = 1, max = 500) String address,
		@Size(max = 50) String propertyType,
		@Size(min = 3, max = 3) String currency) {
}
