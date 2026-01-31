package com.example.flea_market_app.catalog.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListViewDto {

	private UUID id;
	private String name;
	private String price;
	private String imageUrl;
	private String category;
	private boolean soldOut;
}
