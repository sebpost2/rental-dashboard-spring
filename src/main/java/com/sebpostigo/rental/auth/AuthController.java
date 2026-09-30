package com.sebpostigo.rental.auth;

import com.sebpostigo.rental.security.AuthCookieFactory;
import com.sebpostigo.rental.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	private final AuthCookieFactory cookies;

	public AuthController(AuthService authService, AuthCookieFactory cookies) {
		this.authService = authService;
		this.cookies = cookies;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	/** Form-encoded like FastAPI's OAuth2PasswordRequestForm, which the frontend already sends. */
	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public ResponseEntity<TokenResponse> login(@RequestParam String username, @RequestParam String password) {
		String token = authService.login(username, password);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookies.issue(token).toString())
			.body(new TokenResponse(token, "bearer"));
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout() {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
			.body(Map.of("status", "ok"));
	}

	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
		return authService.me(CurrentUser.id(jwt));
	}

}
