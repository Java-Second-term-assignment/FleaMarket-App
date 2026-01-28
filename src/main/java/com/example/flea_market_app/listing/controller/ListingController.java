package com.example.flea_market_app.listing.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.common.response.ApiResponse;
import com.example.flea_market_app.listing.service.ListingService;
import com.example.flea_market_app.listing.service.dto.CreateItemRequest;
import com.example.flea_market_app.listing.service.dto.CreateItemResponse;

import lombok.RequiredArgsConstructor;

/**
 * 商品出品に関するコントローラー。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/listings")
@Validated
public class ListingController {

	private static final Logger log = LoggerFactory.getLogger(ListingController.class);

	private final ListingService listingService;

	/**
	 * 商品を作成します。
	 * 
	 * <p>商品情報と画像をmultipart/form-dataで同時に送信します。
	 * 商品はDRAFT状態で作成されます。
	 * 画像は1-10枚までアップロード可能です。
	 * 
	 * @param request 商品作成リクエスト（JSON形式で"item"パートに含める）
	 * @param images アップロードする画像ファイルのリスト（"images"パートに含める）
	 * @return 商品作成レスポンス（商品ID、画像URLリスト）
	 */
	@PostMapping
	public ResponseEntity<ApiResponse<CreateItemResponse>> createItem(
			@RequestPart("item") @Validated CreateItemRequest request,
			@RequestPart(value = "images", required = false) List<MultipartFile> images) {

		log.info("Received item creation request: name={}, images={}", 
				request.getName(), images != null ? images.size() : 0);

		UUID sellerId = SecurityUtil.getCurrentUserId();
		CreateItemResponse response = listingService.createItem(sellerId, request, images);

		log.info("Successfully created item: {}", response.getItemId());
		return ResponseEntity.ok(ApiResponse.success(response));
	}
}
