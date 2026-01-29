package com.example.flea_market_app.engagement.board.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class CreateBoardPostRequest {

	@NotBlank(message = "本文は必須です")
	@Size(max = 1000, message = "本文は1000文字以内で入力してください")
	private String content;
}
