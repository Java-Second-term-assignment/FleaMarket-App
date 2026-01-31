package com.example.flea_market_app.config.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationConfig {

	public void configureApi(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth
				.requestMatchers("/auth/**").permitAll()
				.requestMatchers("/health").permitAll()
				.requestMatchers(HttpMethod.GET, "/community/boards/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/community/boards/**").hasRole("USER")
				// 商品出品APIは /api/listings（ListingController）で提供
				.requestMatchers("/user/me/**").hasRole("USER")
				.requestMatchers("/api/favorites/**").hasRole("USER")
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated();
	}

	public void configureWeb(
			AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
		auth
				.requestMatchers("/login", "/register", "/password/forgot", "/password-reset-request", "/terms").permitAll()
				.requestMatchers("/", "/products", "/products/**", "/board", "/board/**").permitAll()
				.requestMatchers("/user/settings", "/product/add", "/items/add", "/product/submit", "/order/confirm").authenticated()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/password/change", "/password-change").authenticated()
				.anyRequest().authenticated();
	}
}
