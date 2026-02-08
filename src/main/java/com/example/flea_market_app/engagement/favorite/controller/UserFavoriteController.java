package com.example.flea_market_app.engagement.favorite.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.AddFavoriteRequest;

import lombok.RequiredArgsConstructor;

/**
 * お気に入り用エンドポイント（セッション認証）。
 * 商品詳細画面から fetch で呼び出す。JWT 不要。
 *
 * <p>このコントローラはタイムリーフを想定していない。セッション認証専用。
 * 既存の product_detail.js が同一オリジンで利用。
 * タイムリーフ利用時は JWT 対応の API（例: /api/favorites）の利用を検討すること。
 */
@RestController
@RequestMapping("/user/favorites")
@RequiredArgsConstructor
@Validated
public class UserFavoriteController {

	private final FavoriteService favoriteService;

	@PostMapping
	public ResponseEntity<Void> add(@Valid @RequestBody AddFavoriteRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		favoriteService.add(userId, request.getItemId());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{itemId}")
	public ResponseEntity<Void> remove(@PathVariable UUID itemId) {
		UUID userId = SecurityUtil.getCurrentUserId();
		favoriteService.remove(userId, itemId);
		return ResponseEntity.noContent().build();
	}
}
