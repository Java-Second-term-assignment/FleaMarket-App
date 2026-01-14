package com.example.flea_market_app.config.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationConfig {

	public void configure(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth
				// 公開
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers("/health").permitAll()

				// ユーザー
				.requestMatchers(HttpMethod.POST, "/items/**").hasRole("USER")
				.requestMatchers(HttpMethod.PUT, "/items/**").hasRole("USER")

				// 管理者
				.requestMatchers("/admin/**").hasRole("ADMIN")

				.anyRequest().authenticated();
	}

}
