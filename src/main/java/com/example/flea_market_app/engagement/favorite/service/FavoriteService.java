package com.example.flea_market_app.engagement.favorite.service;

import java.util.List;
import java.util.UUID;

import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;

public interface FavoriteService {

	/**
	 * お気に入りに追加する。公開中（PUBLISHED）の商品のみ追加可能。
	 *
	 * @param userId 認証ユーザーID
	 * @param itemId 商品ID
	 * @throws com.example.flea_market_app.common.exception.NotFoundBusinessException 商品が存在しない場合
	 * @throws com.example.flea_market_app.common.exception.ValidationBusinessException 商品が非公開、または既にお気に入り済みの場合
	 */
	void add(UUID userId, UUID itemId);

	/**
	 * お気に入りから削除する。未登録の場合は何もしない（冪等）。
	 *
	 * @param userId 認証ユーザーID
	 * @param itemId 商品ID
	 */
	void remove(UUID userId, UUID itemId);

	/**
	 * 認証ユーザーのお気に入り一覧を取得する（お気に入り登録日時の新しい順）。
	 *
	 * @param userId 認証ユーザーID
	 * @return お気に入り商品サマリのリスト
	 */
	List<FavoriteItemResponse> listByUser(UUID userId);
}
