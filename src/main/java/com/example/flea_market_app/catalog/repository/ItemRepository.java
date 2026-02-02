package com.example.flea_market_app.catalog.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.catalog.domain.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status")
	Page<ItemEntity> findByStatus(@Param("status") String status, Pageable pageable);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status AND i.categoryId = :categoryId")
	Page<ItemEntity> findByStatusAndCategoryId(@Param("status") String status, @Param("categoryId") UUID categoryId,
			Pageable pageable);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status " +
			"AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
			"OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
	Page<ItemEntity> findByStatusAndKeyword(@Param("status") String status, @Param("keyword") String keyword,
			Pageable pageable);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status " +
			"AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
			"OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
			"AND i.categoryId = :categoryId")
	Page<ItemEntity> findByStatusAndKeywordAndCategoryId(
			@Param("status") String status, @Param("keyword") String keyword,
			@Param("categoryId") UUID categoryId, Pageable pageable);

	@Modifying
	@Query("""
			    UPDATE ItemEntity i
			       SET i.status = :status
			     WHERE i.id = :itemId
			""")
	int updateStatus(
			@Param("itemId") UUID itemId,
			@Param("status") String status);

}
