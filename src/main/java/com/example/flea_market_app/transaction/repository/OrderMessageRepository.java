package com.example.flea_market_app.transaction.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.transaction.domain.OrderMessageEntity;

public interface OrderMessageRepository extends JpaRepository<OrderMessageEntity, UUID> {
	List<OrderMessageEntity> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
