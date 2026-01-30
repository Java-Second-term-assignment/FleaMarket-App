package com.example.flea_market_app.catalog.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.flea_market_app.catalog.service.dto.ItemSummary;

/**
 * 商品の参照・存在チェックを行うサービスのインターフェース。
 */
public interface ItemQueryService {

	/**
	 * 商品が存在することを表明する。存在しなければ NotFoundBusinessException をスローする。
	 *
	 * @param itemId 商品ID
	 */
	void assertExists(UUID itemId);

	/**
	 * 商品が存在し、かつ公開中（PUBLISHED）であることを表明する。
	 * 存在しなければ NotFoundBusinessException、公開中でなければ ValidationBusinessException をスローする。
	 *
	 * @param itemId 商品ID
	 */
	void assertExistsAndPublished(UUID itemId);

	/**
	 * 商品サマリを1件取得する。
	 *
	 * @param itemId 商品ID
	 * @return 存在する場合は ItemSummary、存在しない場合は empty
	 */
	Optional<ItemSummary> findItemSummary(UUID itemId);

	/**
	 * 複数商品のサマリを一括取得する（N+1 回避用）。存在しない ID は結果に含めない。
	 *
	 * @param itemIds 商品IDのコレクション
	 * @return ItemSummary のリスト（存在した商品のみ、順序は保証しない）
	 */
	List<ItemSummary> findItemSummaries(Collection<UUID> itemIds);
}
