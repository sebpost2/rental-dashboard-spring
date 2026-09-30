package com.sebpostigo.rental.auth;

public record UserResponse(long id, String email) {

	static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getEmail());
	}

}
