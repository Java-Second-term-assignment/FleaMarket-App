package com.example.flea_market_app.config.cors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
	private String allowedOriginsConfig;

	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration config = new CorsConfiguration();

		List<String> origins = Arrays.stream(allowedOriginsConfig.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList());
		if (!origins.isEmpty()) {
			config.setAllowedOrigins(origins);
		}

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/api/**", config);
		source.registerCorsConfiguration("/auth/**", config);
		source.registerCorsConfiguration("/user/**", config);
		source.registerCorsConfiguration("/orders/**", config);
		source.registerCorsConfiguration("/community/**", config);

		return source;
	}

}