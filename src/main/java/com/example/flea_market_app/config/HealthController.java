package com.example.flea_market_app.config;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ALB/ECS 等のヘルスチェック用エンドポイント。
 * GET /health で 200 と {"status":"UP"} を返す。
 */
@RestController
public class HealthController {

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		return ResponseEntity.ok(Map.of("status", "UP"));
	}
}
