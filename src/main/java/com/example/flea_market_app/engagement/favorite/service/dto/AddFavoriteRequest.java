package com.example.flea_market_app.engagement.favorite.service.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AddFavoriteRequest {

	@NotNull(message = "商品IDは必須です")
	private UUID itemId;
}
