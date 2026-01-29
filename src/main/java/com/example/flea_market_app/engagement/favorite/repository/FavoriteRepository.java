package com.example.flea_market_app.engagement.favorite.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.engagement.favorite.domain.FavoriteEntity;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {

	boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

	List<FavoriteEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);

	void deleteByUserIdAndItemId(UUID userId, UUID itemId);
}
