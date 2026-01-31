package com.example.flea_market_app.user.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItemViewDto {

	private UUID id;
	private String imageUrl;
	private String title;
	private String price;
}
