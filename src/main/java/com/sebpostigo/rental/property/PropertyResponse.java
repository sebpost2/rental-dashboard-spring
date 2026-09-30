package com.sebpostigo.rental.property;

public record PropertyResponse(long id, String name, String address, String propertyType, String currency) {

	static PropertyResponse from(Property property) {
		return new PropertyResponse(property.getId(), property.getName(), property.getAddress(),
				property.getPropertyType(), property.getCurrency());
	}

}
