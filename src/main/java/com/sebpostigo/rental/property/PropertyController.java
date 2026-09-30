package com.sebpostigo.rental.property;

import com.sebpostigo.rental.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties")
public class PropertyController {

	private final PropertyService propertyService;

	public PropertyController(PropertyService propertyService) {
		this.propertyService = propertyService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PropertyResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PropertyCreateRequest request) {
		return propertyService.create(CurrentUser.id(jwt), request);
	}

	@GetMapping
	public List<PropertyResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return propertyService.list(CurrentUser.id(jwt));
	}

	@GetMapping("/{propertyId}")
	public PropertyResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId) {
		return propertyService.get(CurrentUser.id(jwt), propertyId);
	}

	@PatchMapping("/{propertyId}")
	public PropertyResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId,
			@Valid @RequestBody PropertyUpdateRequest request) {
		return propertyService.update(CurrentUser.id(jwt), propertyId, request);
	}

	@DeleteMapping("/{propertyId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable long propertyId) {
		propertyService.delete(CurrentUser.id(jwt), propertyId);
	}

}
