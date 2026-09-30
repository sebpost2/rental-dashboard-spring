package com.sebpostigo.rental.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

	@Bean
	OpenAPI rentalOpenApi() {
		SecurityScheme cookieAuth = new SecurityScheme().type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.COOKIE)
			.name("access_token")
			.description("Set by POST /auth/login");
		return new OpenAPI()
			.info(new Info().title("Rental Dashboard API")
				.version("1.0")
				.description("Spring Boot edition of the Rental Dashboard backend."))
			.components(new Components().addSecuritySchemes("cookieAuth", cookieAuth))
			.addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
	}

}
