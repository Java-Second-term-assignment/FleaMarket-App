package com.example.flea_market_app.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.catalog.domain.ItemImageEntity;

public interface ItemImageRepository extends JpaRepository<ItemImageEntity, UUID> {

	/**
	 * 商品IDで画像を取得（表示順でソート）
	 * 
	 * @param itemId 商品ID
	 * @return 画像リスト（display_orderの昇順）
	 */
	List<ItemImageEntity> findByItemIdOrderByDisplayOrderAsc(UUID itemId);

	/**
	 * 商品の画像数をカウント
	 * 
	 * @param itemId 商品ID
	 * @return 画像数
	 */
	long countByItemId(UUID itemId);

	/**
	 * 商品の全画像を削除
	 * 
	 * @param itemId 商品ID
	 */
	void deleteByItemId(UUID itemId);
}
