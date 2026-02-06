package com.example.flea_market_app.support;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.integration.aws.ComprehendClientPort;
import com.example.flea_market_app.integration.aws.RekognitionClientPort;
import com.example.flea_market_app.integration.aws.dto.ImageModerationResult;
import com.example.flea_market_app.integration.aws.dto.ModerationResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 統合テストの共通基盤。PostgreSQL Testcontainers、外部サービスモック、JWT 取得ヘルパーを提供する。
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

	@Autowired
	protected TestRestTemplate restTemplate;

	@MockitoBean
	protected S3ImageService s3ImageService;

	@MockitoBean
	protected ComprehendClientPort comprehendClientPort;

	@MockitoBean
	protected RekognitionClientPort rekognitionClientPort;

	@DynamicPropertySource
	static void configureFlyway(DynamicPropertyRegistry registry) {
		registry.add("spring.flyway.locations", () -> "classpath:db/migration,classpath:db/test_migration");
	}

	@BeforeEach
	void configureMocks() {
		when(s3ImageService.uploadImage(anyString(), anyString(), any(), anyString(), anyLong()))
				.thenReturn("profiles/test/key.jpg");
		when(s3ImageService.generateImageUrl(anyString(), anyString()))
				.thenReturn("https://example.com/image.jpg");

		when(comprehendClientPort.analyzeText(anyString()))
				.thenReturn(new ModerationResult(false, 0.0, List.of()));

		when(rekognitionClientPort.analyzeImage(anyString()))
				.thenReturn(new ImageModerationResult(false, false, 0.0, List.of()));
	}

	/**
	 * ログインしてアクセストークンを取得する。
	 *
	 * @param email    ログイン用メールアドレス（例: testuser@example.com）
	 * @param password パスワード（例: password）
	 * @return アクセストークン。認証失敗時は null
	 */
	@SuppressWarnings("unchecked")
	protected String obtainAccessToken(String email, String password) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> request = new HttpEntity<>(
				Map.of("identifier", email, "password", password),
				headers);

		var response = restTemplate.postForEntity("/auth/login", request, Map.class);
		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return (String) response.getBody().get("accessToken");
		}
		return null;
	}

	/**
	 * Bearer トークン付きの HttpHeaders を返す。
	 */
	protected HttpHeaders authHeaders(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}
