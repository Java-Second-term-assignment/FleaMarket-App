package com.example.flea_market_app.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final AuthorizationConfig authorizationConfig;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AdminLoginSuccessHandler adminLoginSuccessHandler() {
		return new AdminLoginSuccessHandler();
	}

	/**
	 * 管理者用フォームログイン: /admin/login のみを扱う（一般ユーザーとエントリーポイントを分離）
	 */
	@Bean
	@Order(1)
	SecurityFilterChain adminLoginSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/admin/login", "/admin/login*")
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(
						org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
				.formLogin(form -> form
						.loginPage("/admin/login")
						.loginProcessingUrl("/admin/login")
						.usernameParameter("email")
						.passwordParameter("password")
						.successHandler(adminLoginSuccessHandler())
						.failureUrl("/admin/login?error"));

		return http.build();
	}

	/**
	 * API用: JWT認証、stateless、JSONエラーレスポンス
	 */
	@Bean
	@Order(2)
	SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/api/**", "/auth/**", "/community/**", "/orders/**")
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> authorizationConfig.configureApi(auth))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							response.setStatus(401);
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write("""
									{
									  "error": "UNAUTHORIZED",
									  "message": "認証が必要です"
									}
									""");
						})
						.accessDeniedHandler((request, response, accessDeniedException) -> {
							response.setStatus(403);
							response.setContentType("application/json;charset=UTF-8");
							response.getWriter().write("""
									{
									  "error": "FORBIDDEN",
									  "message": "権限がありません"
									}
									""");
						}));

		return http.build();
	}

	/**
	 * Web用: フォーム認証、セッション、Thymeleafページ（一般ユーザーは /login）
	 */
	@Bean
	@Order(3)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/**")
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(
						org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> authorizationConfig.configureWeb(auth))
				.formLogin(form -> form
						.loginPage("/login")
						.defaultSuccessUrl("/", false)
						.usernameParameter("email")
						.passwordParameter("password"))
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/login")
						.invalidateHttpSession(true));

		return http.build();
	}
}
