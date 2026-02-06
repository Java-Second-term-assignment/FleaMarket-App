package com.example.flea_market_app.config.security;

import jakarta.servlet.DispatcherType;

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
				.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
				.requestMatchers("/css/**", "/js/**", "/img/**", "/fonts/**", "/*.ico").permitAll()
				.requestMatchers("/webhooks/**").permitAll()
				.requestMatchers("/login", "/register", "/password/forgot", "/password-reset-request", "/password/reset", "/terms").permitAll()
				.requestMatchers("/admin/login").permitAll()
				.requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
				.requestMatchers("/", "/products", "/products/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/board", "/board/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/board/*/posts").authenticated()
				.requestMatchers("/user/settings", "/user/settings/**", "/user/address/**", "/user/orders", "/user/orders/**", "/user/favorites", "/user/favorites/**", "/product/add", "/items/add", "/product/submit", "/product/*/review", "/products/*/report", "/order/confirm", "/payment", "/address", "/cart", "/cart/**").authenticated()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/password/change", "/password-change").authenticated()
				.anyRequest().authenticated();
	}
}
