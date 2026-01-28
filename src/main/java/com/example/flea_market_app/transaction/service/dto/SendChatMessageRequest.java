package com.example.flea_market_app.transaction.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Jackson用
public class SendChatMessageRequest {

	@NotBlank(message = "error.chat_message.required")
	@Size(max = 4000, message = "error.chat_message.too_long")
	private String content;
}
