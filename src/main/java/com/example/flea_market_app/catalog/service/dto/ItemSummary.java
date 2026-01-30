package com.example.flea_market_app.catalog.service.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSummary {

	private UUID id;
	private String name;
	private Long priceAmount;
	private String currency;
	private String status;
}
