package com.example.flea_market_app.config.security;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
		AdminLoginSuccessHandler handler = new AdminLoginSuccessHandler();
		handler.setDefaultTargetUrl("/admin/dashboard");
		handler.setAlwaysUseDefaultTargetUrl(true);
		return handler;
	}

	@Bean
	WebLoginSuccessHandler webLoginSuccessHandler() {
		WebLoginSuccessHandler handler = new WebLoginSuccessHandler();
		handler.setDefaultTargetUrl("/products");
		return handler;
	}

	/**
	 * 管理者用フォームログイン: /admin/login のみを扱う（一般ユーザーとエントリーポイントを分離）
	 */
	@Bean
	@Order(1)
	SecurityFilterChain adminLoginSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/admin/login", "/admin/login*")
				// CSRF 有効（フォームログインのため）
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
	 * 未認証時はログイン画面へ returnUrl 付きでリダイレクト（戻る・ログイン後に元の画面へ遷移可能）。
	 */
	@Bean
	@Order(3)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.securityMatcher("/**")
				// CSRF 有効（フォーム・状態変更系を保護）
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(
						org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
				.authorizeHttpRequests(auth -> authorizationConfig.configureWeb(auth))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint((request, response, authException) -> {
							String requestUri = request.getRequestURI();
							String queryString = request.getQueryString();
							String fullPath = requestUri + (queryString != null && !queryString.isEmpty() ? "?" + queryString : "");
							// 相対パスのみ許可（オープンリダイレクト対策）
							boolean safe = fullPath.startsWith("/") && !fullPath.startsWith("//");
							String returnUrl = safe ? URLEncoder.encode(fullPath, StandardCharsets.UTF_8) : "";
							String loginPath = request.getContextPath() + "/login";
							String redirect = returnUrl.isEmpty() ? loginPath : loginPath + "?returnUrl=" + returnUrl;
							response.sendRedirect(redirect);
						}))
				.formLogin(form -> form
						.loginPage("/login")
						.successHandler(webLoginSuccessHandler())
						.usernameParameter("email")
						.passwordParameter("password"))
				.logout(logout -> logout
						.logoutUrl("/logout")
						.logoutSuccessUrl("/products")
						.invalidateHttpSession(true));

		return http.build();
	}
}
