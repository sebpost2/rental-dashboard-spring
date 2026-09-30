package com.sebpostigo.rental;

import org.springframework.boot.SpringApplication;

public class TestRentalDashboardSpringApplication {

	public static void main(String[] args) {
		SpringApplication.from(RentalDashboardSpringApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
