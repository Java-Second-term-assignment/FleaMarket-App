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
 * 取引チャット用エンドポイント（セッション認証）。
 * 注文詳細画面から fetch で呼び出す。JWT 不要。
 */
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
@Validated
public class OrderMessageController {

	private final ChatService chatService;

	@GetMapping("/{orderId}/messages")
	public ResponseEntity<List<OrderMessageEntity>> list(@PathVariable UUID orderId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(chatService.listMessages(orderId, userId));
	}

	@PostMapping("/{orderId}/messages")
	public ResponseEntity<Void> send(
			@PathVariable UUID orderId,
			@Valid @RequestBody SendChatMessageRequest req) {
		UUID userId = SecurityUtil.getCurrentUserId();
		chatService.sendMessage(orderId, userId, req.getContent());
		return ResponseEntity.noContent().build();
	}
}
