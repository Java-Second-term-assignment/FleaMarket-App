package com.example.flea_market_app.engagement.board.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.engagement.board.domain.BoardPostEntity;

public interface BoardPostRepository extends JpaRepository<BoardPostEntity, UUID> {

	List<BoardPostEntity> findByItemIdOrderByCreatedAtDesc(UUID itemId, Pageable pageable);
}
