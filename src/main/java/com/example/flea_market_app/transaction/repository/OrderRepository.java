package com.example.flea_market_app.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.transaction.domain.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

	boolean existsByItemId(UUID itemId);

	List<OrderEntity> findByBuyerIdOrderByUpdatedAtDesc(UUID buyerId);

	List<OrderEntity> findBySellerIdOrderByUpdatedAtDesc(UUID sellerId);

	Optional<OrderEntity> findByItemId(UUID itemId);

	Optional<OrderEntity> findByItemIdAndStatus(UUID itemId, String status);

	/**
	 * 同一商品で指定ステータスの注文を、作成日時の新しい順で取得する。
	 * 1商品が複数回売れた場合に複数件返る。
	 */
	List<OrderEntity> findByItemIdAndStatusOrderByCreatedAtDesc(UUID itemId, String status);
}
