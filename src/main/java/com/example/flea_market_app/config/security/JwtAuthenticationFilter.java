package com.example.flea_market_app.config.security;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.flea_market_app.auth.service.JwtSupport;
import com.example.flea_market_app.auth.service.UserIdUserDetails;
import com.example.flea_market_app.auth.service.UserIdUserDetailsLoader;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtSupport jwtSupport;
	private final UserIdUserDetailsLoader userIdUserDetailsLoader;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		Optional<String> tokenOpt = extractBearerToken(request);
		if (tokenOpt.isEmpty()) {
			filterChain.doFilter(request, response);
			return;
		}

		Optional<UUID> userIdOpt = jwtSupport.parseUserIdFromAccessToken(tokenOpt.get());
		if (userIdOpt.isEmpty()) {
			sendUnauthorized(response);
			return;
		}

		Optional<UserIdUserDetails> userDetailsOpt = userIdUserDetailsLoader.loadByUserId(userIdOpt.get());
		if (userDetailsOpt.isEmpty()) {
			sendUnauthorized(response);
			return;
		}

		UserIdUserDetails principal = userDetailsOpt.get();
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

	private Optional<String> extractBearerToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			return Optional.empty();
		}
		String token = header.substring(BEARER_PREFIX.length()).trim();
		return token.isEmpty() ? Optional.empty() : Optional.of(token);
	}

	private void sendUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("""
				{
				  "error": "UNAUTHORIZED",
				  "message": "認証が必要です"
				}
				""");
	}
}
