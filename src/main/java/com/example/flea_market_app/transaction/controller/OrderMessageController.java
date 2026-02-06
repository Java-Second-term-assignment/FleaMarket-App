package com.example.flea_market_app.transaction.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.common.response.ApiResponse;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.transaction.service.ChatService;
import com.example.flea_market_app.transaction.service.dto.ChatMessageResponse;
import com.example.flea_market_app.transaction.service.dto.SendChatMessageRequest;

import lombok.RequiredArgsConstructor;

/**
 * 取引チャット用エンドポイント（セッション認証）。
 * 注文詳細画面から fetch で呼び出す。JWT 不要。
 * レスポンスは ApiResponse でラップ（success/data 形式）。フロントは response.data を参照すること。
 */
@RestController
@RequestMapping("/user/orders")
@RequiredArgsConstructor
@Validated
public class OrderMessageController {

	private static final int DEFAULT_MESSAGE_SIZE = 50;

	private final ChatService chatService;

	@GetMapping("/{orderId}/messages")
	public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> list(
			@PathVariable UUID orderId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "" + DEFAULT_MESSAGE_SIZE) @Min(1) @Max(100) int size) {
		UUID userId = SecurityUtil.getCurrentUserId();
		List<ChatMessageResponse> messages = chatService.listMessages(orderId, userId, page, size);
		return ResponseEntity.ok(ApiResponse.success(messages));
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
