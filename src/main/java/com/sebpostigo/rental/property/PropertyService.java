package com.sebpostigo.rental.property;

import com.sebpostigo.rental.common.NotFoundException;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PropertyService {

	static final String DEFAULT_PROPERTY_TYPE = "apartment";

	static final String DEFAULT_CURRENCY = "USD";

	private final PropertyRepository properties;

	public PropertyService(PropertyRepository properties) {
		this.properties = properties;
	}

	@Transactional
	public PropertyResponse create(long ownerId, PropertyCreateRequest request) {
		Property property = new Property(ownerId, request.name(), request.address(),
				Objects.requireNonNullElse(request.propertyType(), DEFAULT_PROPERTY_TYPE),
				Objects.requireNonNullElse(request.currency(), DEFAULT_CURRENCY));
		return PropertyResponse.from(properties.save(property));
	}

	public List<PropertyResponse> list(long ownerId) {
		return properties.findByOwnerIdOrderByIdAsc(ownerId).stream().map(PropertyResponse::from).toList();
	}

	public PropertyResponse get(long ownerId, long propertyId) {
		return PropertyResponse.from(requireOwned(ownerId, propertyId));
	}

	@Transactional
	public PropertyResponse update(long ownerId, long propertyId, PropertyUpdateRequest request) {
		Property property = requireOwned(ownerId, propertyId);
		property.update(request.name(), request.address(), request.propertyType(), request.currency());
		return PropertyResponse.from(property);
	}

	/** Incomes and expenses go with it via ON DELETE CASCADE. */
	@Transactional
	public void delete(long ownerId, long propertyId) {
		properties.delete(requireOwned(ownerId, propertyId));
	}

	/** Not-found and not-yours are the same 404, so the API never reveals which ids exist. */
	public Property requireOwned(long ownerId, long propertyId) {
		return properties.findByIdAndOwnerId(propertyId, ownerId)
			.orElseThrow(() -> new NotFoundException("Property not found"));
	}

}
