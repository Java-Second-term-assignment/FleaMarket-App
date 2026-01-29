package com.example.flea_market_app.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {

		registry.addMapping("/api/**")
				.allowedOrigins(
						"http://localhost:3000",
						"http://example.com")
				.allowedMethods(
						"GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders(
						"Authorization",
						"Content-Type")
				.allowCredentials(true)
				.maxAge(3600);

		registry.addMapping("/community/**")
				.allowedOrigins(
						"http://localhost:3000",
						"http://example.com")
				.allowedMethods(
						"GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders(
						"Authorization",
						"Content-Type")
				.allowCredentials(true)
				.maxAge(3600);
	}
}