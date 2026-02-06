package com.example.flea_market_app.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.transaction.domain.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

	List<OrderEntity> findByBuyerIdOrderByUpdatedAtDesc(UUID buyerId);

	List<OrderEntity> findBySellerIdOrderByUpdatedAtDesc(UUID sellerId);

	Optional<OrderEntity> findByItemIdAndStatus(UUID itemId, String status);
}
