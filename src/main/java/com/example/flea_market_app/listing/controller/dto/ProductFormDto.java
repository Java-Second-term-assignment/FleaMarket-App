package com.example.flea_market_app.listing.controller.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductFormDto {

	@NotBlank(message = "商品名は必須です")
	@Size(max = 200)
	private String name;

	@Size(max = 5000)
	private String description;

	@NotNull(message = "価格は必須です")
	@Min(value = 300)
	private Long price;

	@NotNull(message = "カテゴリは必須です")
	private UUID categoryId;

	@NotBlank
	@Pattern(regexp = "^(NEW|LIKE_NEW|USED_GOOD|USED_FAIR|USED_POOR)$")
	private String condition;

	@NotBlank
	@Pattern(regexp = "^(SELLER|BUYER)$")
	private String shippingFeePayer;
}
