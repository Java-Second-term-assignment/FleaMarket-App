package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.domain.OrderMessageEntity;
import com.example.flea_market_app.transaction.service.ChatService;
import com.example.flea_market_app.transaction.service.dto.SendChatMessageRequest;

import lombok.RequiredArgsConstructor;

/**
 * 取引に関するコミュニケーションの入口。
 *
 * 絶対ルール:
 * - orderId 必須
 * - userId は context から取得（リクエストで受け取らない）
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Validated
public class ChatController {

	private final ChatService chatService;

	/** 送信: POST /orders/{orderId}/messages */
	@PostMapping("/{orderId}/messages")
	public ResponseEntity<Void> send(
			@PathVariable UUID orderId,
			@Valid @RequestBody SendChatMessageRequest req) {
		UUID userId = SecurityUtil.getCurrentUserId();
		chatService.sendMessage(orderId, userId, req.getContent());
		return ResponseEntity.noContent().build();
	}

	/**
	 * 一覧: GET /orders/{orderId}/messages
	 * MVPで不要ならコメントアウトしてOK（Service側は残しても害はない）
	 */
	@GetMapping("/{orderId}/messages")
	public ResponseEntity<List<OrderMessageEntity>> list(@PathVariable UUID orderId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(chatService.listMessages(orderId, userId));
	}
}
