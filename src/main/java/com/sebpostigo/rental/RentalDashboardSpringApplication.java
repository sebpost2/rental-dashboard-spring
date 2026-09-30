package com.sebpostigo.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class RentalDashboardSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(RentalDashboardSpringApplication.class, args);
	}

}
