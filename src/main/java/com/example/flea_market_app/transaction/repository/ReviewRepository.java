package com.example.flea_market_app.transaction.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.transaction.domain.ReviewEntity;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
	boolean existsByOrderIdAndReviewerId(UUID orderId, UUID reviewerId);
}
