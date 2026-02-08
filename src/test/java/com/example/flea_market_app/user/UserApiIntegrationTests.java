package com.example.flea_market_app.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.example.flea_market_app.support.IntegrationTestBase;

class UserApiIntegrationTests extends IntegrationTestBase {

	@Nested
	class GetMe {

		@Test
		void getMe_withValidJwt_returns200() {
			String token = obtainAccessToken("testuser@example.com", "password");
			assertThat(token).isNotNull();

			HttpHeaders headers = authHeaders(token);
			var response = restTemplate.exchange(
					"/api/user/me",
					HttpMethod.GET,
					new HttpEntity<>(headers),
					Map.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).containsKeys("userId", "displayName", "email");
			assertThat(response.getBody().get("userId").toString()).isEqualTo("40000000-0000-0000-0000-000000000001");
			assertThat(response.getBody().get("displayName")).isNotNull();
			assertThat(response.getBody().get("email")).isEqualTo("testuser@example.com");
		}

		@Test
		void getMe_withoutAuth_returns401() {
			var response = restTemplate.getForEntity("/api/user/me", Map.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
			assertThat(response.getBody()).containsKey("error");
		}

		@Test
		void getMe_withInvalidJwt_returns401() {
			HttpHeaders headers = authHeaders("invalid.jwt.token");
			var response = restTemplate.exchange(
					"/api/user/me",
					HttpMethod.GET,
					new HttpEntity<>(headers),
					Map.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		}
	}

	@Nested
	class UpdateMe {

		@Test
		void updateMe_withValidJwt_returns204() {
			String token = obtainAccessToken("testuser@example.com", "password");
			assertThat(token).isNotNull();

			HttpHeaders headers = authHeaders(token);
			HttpEntity<Map<String, String>> request = new HttpEntity<>(
					Map.of("displayName", "UpdatedName", "caption", "New caption"),
					headers);

			var response = restTemplate.exchange(
					"/api/user/me",
					HttpMethod.PATCH,
					request,
					Void.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

			// 更新が反映されていることを確認
			var getResponse = restTemplate.exchange(
					"/api/user/me",
					HttpMethod.GET,
					new HttpEntity<>(headers),
					Map.class);
			assertThat(getResponse.getBody().get("displayName")).isEqualTo("UpdatedName");
			assertThat(getResponse.getBody().get("caption")).isEqualTo("New caption");
		}
	}
}
