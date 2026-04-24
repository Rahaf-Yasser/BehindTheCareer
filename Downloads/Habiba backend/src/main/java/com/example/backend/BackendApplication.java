package com.example.backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import jakarta.servlet.Filter;

@SpringBootApplication
@Slf4j
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			log.info("=== Registered Filters ===");
			String[] filterNames = ctx.getBeanNamesForType(Filter.class);
			for (String filterName : filterNames) {
				log.info("Filter: {}", filterName);
			}

			// Specifically check if JWTFilter is registered
			boolean jwtFilterFound = false;
			for (String filterName : filterNames) {
				if (filterName.toLowerCase().contains("jwt")) {
					jwtFilterFound = true;
					log.info("✅ JWTFilter found: {}", filterName);
				}
			}

			if (!jwtFilterFound) {
				log.error("❌ JWTFilter NOT found in registered filters!");
			}
		};
	}
}