package com.example.flea_market_app.catalog.controller.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailViewDto {

	private UUID id;
	private String name;
	private String description;
	private Long price;
	private String mainImageUrl;
	private List<ProductImageDto> images;
	private CategoryDisplayDto category;
	private int stock;
	/** 出品者ID（注文確認時の手数料計算に使用） */
	private UUID sellerId;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ProductImageDto {
		private String thumbnailUrl;
		private String largeUrl;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CategoryDisplayDto {
		private String name;
	}
}
