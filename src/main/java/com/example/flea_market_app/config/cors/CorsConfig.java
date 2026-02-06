package com.example.flea_market_app.config.cors;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of(
				"http://localhost:3000",
				"http://example.com"

		));

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