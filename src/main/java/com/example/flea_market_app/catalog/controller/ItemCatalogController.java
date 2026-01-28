package com.example.flea_market_app.catalog.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 商品カタログに関するコントローラー。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Validated
public class ItemCatalogController {

	private static final Logger log = LoggerFactory.getLogger(ItemCatalogController.class);

	private final ItemImageService itemImageService;

	/**
	 * 商品の全画像URLを取得します。
	 * 
	 * <p>画像は表示順（display_orderの昇順）で返されます。
	 * 
	 * @param itemId 商品ID
	 * @return 画像URLのリスト
	 */
	@GetMapping("/{itemId}/images")
	public ResponseEntity<ApiResponse<List<String>>> getItemImages(@PathVariable UUID itemId) {
		log.info("Getting images for item: {}", itemId);

		List<String> imageUrls = itemImageService.getItemImageUrls(itemId);

		log.info("Found {} images for item: {}", imageUrls.size(), itemId);
		return ResponseEntity.ok(ApiResponse.success(imageUrls));
	}

	/**
	 * サムネイル画像（1枚目）のURLを取得します。
	 * 
	 * @param itemId 商品ID
	 * @return サムネイル画像のURL。画像が存在しない場合はnull
	 */
	@GetMapping("/{itemId}/thumbnail")
	public ResponseEntity<ApiResponse<String>> getThumbnailImage(@PathVariable UUID itemId) {
		log.info("Getting thumbnail image for item: {}", itemId);

		String thumbnailUrl = itemImageService.getThumbnailImageUrl(itemId);

		log.info("Thumbnail image for item {}: {}", itemId, thumbnailUrl != null ? "found" : "not found");
		return ResponseEntity.ok(ApiResponse.success(thumbnailUrl));
	}
}
