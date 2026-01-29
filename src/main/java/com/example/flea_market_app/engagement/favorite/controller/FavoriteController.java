package com.example.flea_market_app.engagement.favorite.controller;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.common.response.ApiResponse;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.AddFavoriteRequest;
import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
@Validated
public class FavoriteController {

	private static final Logger log = LoggerFactory.getLogger(FavoriteController.class);

	private final FavoriteService favoriteService;

	/**
	 * お気に入りに追加します。公開中（PUBLISHED）の商品のみ追加可能です。
	 *
	 * @param request 商品IDを含むリクエスト
	 * @return 204 No Content
	 */
	@PostMapping
	public ResponseEntity<Void> add(@RequestBody @Valid AddFavoriteRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Adding favorite: userId={}, itemId={}", userId, request.getItemId());

		favoriteService.add(userId, request.getItemId());

		log.info("Successfully added favorite: userId={}, itemId={}", userId, request.getItemId());
		return ResponseEntity.noContent().build();
	}

	/**
	 * お気に入りから削除します。未登録の場合は何もしません（冪等）。
	 *
	 * @param itemId 商品ID
	 * @return 204 No Content
	 */
	@DeleteMapping("/{itemId}")
	public ResponseEntity<Void> remove(@PathVariable UUID itemId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Removing favorite: userId={}, itemId={}", userId, itemId);

		favoriteService.remove(userId, itemId);

		log.info("Successfully removed favorite: userId={}, itemId={}", userId, itemId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 認証ユーザーのお気に入り一覧を取得します（お気に入り登録日時の新しい順）。
	 *
	 * @return お気に入り商品サマリのリスト
	 */
	@GetMapping
	public ResponseEntity<ApiResponse<List<FavoriteItemResponse>>> list() {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Listing favorites: userId={}", userId);

		List<FavoriteItemResponse> items = favoriteService.listByUser(userId);

		log.info("Found {} favorites for user: {}", items.size(), userId);
		return ResponseEntity.ok(ApiResponse.success(items));
	}
}
