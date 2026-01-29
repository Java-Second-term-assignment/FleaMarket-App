package com.example.flea_market_app.engagement.favorite.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItemResponse {

	private UUID itemId;
	private String name;
	private Long priceAmount;
	private String currency;
	private String thumbnailUrl;
	private String status;
}
