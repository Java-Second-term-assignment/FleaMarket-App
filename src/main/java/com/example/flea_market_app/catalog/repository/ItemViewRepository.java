package com.example.flea_market_app.catalog.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.catalog.domain.ItemViewEntity;

public interface ItemViewRepository extends JpaRepository<ItemViewEntity, UUID> {

	boolean existsByUserIdAndItemId(UUID userId, UUID itemId);

	@Query(value = """
			SELECT iv.item_id FROM item_views iv
			INNER JOIN items i ON iv.item_id = i.id
			WHERE i.status = :status
			GROUP BY iv.item_id
			ORDER BY COUNT(*) DESC
			""", nativeQuery = true)
	Page<Object[]> findTopViewedItemIds(@Param("status") String status, Pageable pageable);
}
