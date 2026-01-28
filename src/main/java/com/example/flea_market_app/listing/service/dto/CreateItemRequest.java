package com.example.flea_market_app.listing.service.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

/**
 * 商品作成リクエストDTO。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Data
public class CreateItemRequest {

	@NotBlank(message = "商品名は必須です")
	@Size(max = 200, message = "商品名は200文字以内で入力してください")
	private String name;

	@Size(max = 5000, message = "説明は5000文字以内で入力してください")
	private String description;

	@NotNull(message = "価格は必須です")
	@Min(value = 0, message = "価格は0以上である必要があります")
	private Long priceAmount;

	@NotNull(message = "カテゴリIDは必須です")
	private UUID categoryId;

	@NotBlank(message = "商品状態は必須です")
	private String condition; // NEW, LIKE_NEW, USED_GOOD, USED_FAIR, USED_POOR

	@NotBlank(message = "配送料負担者は必須です")
	private String shippingFeePayer; // SELLER, BUYER
}
