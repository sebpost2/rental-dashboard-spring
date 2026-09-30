package com.sebpostigo.rental.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.sebpostigo.rental.common.AppProperties;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

	static final String[] PUBLIC_PATHS = { "/health", "/auth/register", "/auth/login", "/auth/logout", "/error",
			"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		ProblemAuthenticationEntryPoint entryPoint = new ProblemAuthenticationEntryPoint();
		http
			// Stateless API: no session to ride, and login/logout are guarded by an Origin check instead.
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC_PATHS).permitAll().anyRequest().authenticated())
			.oauth2ResourceServer(oauth -> oauth.bearerTokenResolver(new CookieBearerTokenResolver())
				.authenticationEntryPoint(entryPoint)
				.jwt(Customizer.withDefaults()))
			.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint));
		return http.build();
	}

	@Bean
	JwtEncoder jwtEncoder(AppProperties props) {
		return jwtEncoderFor(signingKey(props.jwt().secret()));
	}

	@Bean
	JwtDecoder jwtDecoder(AppProperties props) {
		return jwtDecoderFor(signingKey(props.jwt().secret()));
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AppProperties props) {
		CorsConfiguration cors = new CorsConfiguration();
		cors.setAllowedOrigins(props.corsOrigins());
		cors.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		cors.setAllowedHeaders(List.of("*"));
		cors.setExposedHeaders(List.of("Content-Disposition"));
		cors.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", cors);
		return source;
	}

	static SecretKey signingKey(String secret) {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}

	static JwtEncoder jwtEncoderFor(SecretKey key) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	static JwtDecoder jwtDecoderFor(SecretKey key) {
		return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}

}
