package com.sebpostigo.rental.auth;

import com.sebpostigo.rental.common.ApiException;
import com.sebpostigo.rental.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

	private final UserRepository users;

	private final PasswordEncoder passwordEncoder;

	private final JwtService jwtService;

	public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public UserResponse register(RegisterRequest request) {
		if (users.existsByEmail(request.email())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "Email already registered");
		}
		User user = users.save(new User(request.email(), passwordEncoder.encode(request.password())));
		return UserResponse.from(user);
	}

	public String login(String email, String password) {
		User user = users.findByEmail(email)
			.filter(candidate -> passwordEncoder.matches(password, candidate.getHashedPassword()))
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Incorrect email or password"));
		return jwtService.issue(user.getId(), user.getEmail());
	}

	public UserResponse me(long userId) {
		return users.findById(userId)
			.map(UserResponse::from)
			.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Could not validate credentials"));
	}

}
