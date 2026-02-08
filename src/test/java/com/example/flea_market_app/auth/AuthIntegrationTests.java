package com.example.flea_market_app.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.example.flea_market_app.support.IntegrationTestBase;

class AuthIntegrationTests extends IntegrationTestBase {

	@Nested
	class Login {

		@Test
		void login_success_returnsTokens() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("identifier", "testuser@example.com", "password", "password"),
					headers);

			var response = restTemplate.postForEntity("/auth/login", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).containsKeys("userId", "accessToken", "refreshToken");
			assertThat(response.getBody().get("accessToken")).isNotNull();
			assertThat(response.getBody().get("refreshToken")).isNotNull();
			assertThat(response.getBody().get("userId")).isEqualTo("40000000-0000-0000-0000-000000000001");
		}

		@Test
		void login_invalidCredentials_returns401() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("identifier", "testuser@example.com", "password", "wrongpassword"),
					headers);

			var response = restTemplate.postForEntity("/auth/login", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).containsKey("error");
		}

		@Test
		void adminLogin_nonAdmin_returns403() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("identifier", "testuser@example.com", "password", "password"),
					headers);

			var response = restTemplate.postForEntity("/auth/admin/login", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		}

		@Test
		void adminLogin_admin_returnsTokens() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("identifier", "admin@example.com", "password", "password"),
					headers);

			var response = restTemplate.postForEntity("/auth/admin/login", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).containsKeys("userId", "accessToken", "refreshToken");
		}
	}

	@Nested
	class Refresh {

		@Test
		void refresh_validToken_returnsNewTokens() {
			String accessToken = obtainAccessToken("testuser@example.com", "password");
			assertThat(accessToken).isNotNull();

			String refreshToken = loginAndGetRefreshToken("testuser@example.com", "password");
			assertThat(refreshToken).isNotNull();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("refreshToken", refreshToken),
					headers);

			var response = restTemplate.postForEntity("/auth/refresh", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).containsKey("accessToken");
			assertThat(response.getBody().get("accessToken")).isNotNull();
		}

		@Test
		void refresh_invalidToken_returns401() {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("refreshToken", "invalid-refresh-token"),
					headers);

			var response = restTemplate.postForEntity("/auth/refresh", request, Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
	}

	@SuppressWarnings("unchecked")
	private String loginAndGetRefreshToken(String email, String password) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> request = new HttpEntity<>(
				Map.of("identifier", email, "password", password),
				headers);
		var response = restTemplate.postForEntity("/auth/login", request, Map.class);
		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return (String) response.getBody().get("refreshToken");
		}
		return null;
	}
}
