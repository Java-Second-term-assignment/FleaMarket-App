package com.example.flea_market_app.catalog.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.catalog.domain.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

	long countByStatus(String status);

	List<ItemEntity> findAllByStatus(String status);

	List<ItemEntity> findAllByStatusNot(String status);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status")
	Page<ItemEntity> findByStatus(@Param("status") String status, Pageable pageable);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status AND i.categoryId = :categoryId")
	Page<ItemEntity> findByStatusAndCategoryId(@Param("status") String status, @Param("categoryId") UUID categoryId,
			Pageable pageable);

	@Query("SELECT i FROM ItemEntity i WHERE i.status = :status AND i.categoryId = :categoryId AND i.id <> :excludeId")
	Page<ItemEntity> findByStatusAndCategoryIdAndIdNot(@Param("status") String status,
			@Param("categoryId") UUID categoryId, @Param("excludeId") UUID excludeId, Pageable pageable);

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
			       SET i.name = :name, i.priceAmount = :priceAmount
			     WHERE i.id = :itemId
			""")
	int updateNameAndPrice(
			@Param("itemId") UUID itemId,
			@Param("name") String name,
			@Param("priceAmount") Long priceAmount);

	@Modifying
	@Query("""
			    UPDATE ItemEntity i
			       SET i.status = :status
			     WHERE i.id = :itemId
			""")
	int updateStatus(
			@Param("itemId") UUID itemId,
			@Param("status") String status);

	/** ステータス別件数（グラフ用） */
	@Query("SELECT i.status, COUNT(i) FROM ItemEntity i GROUP BY i.status")
	List<Object[]> countGroupByStatus();

	/** 指定日以降の日別商品登録数（グラフ用） */
	@Query(value = """
			SELECT (created_at AT TIME ZONE 'UTC')::date AS day, COUNT(*) FROM items
			WHERE created_at >= :since
			GROUP BY (created_at AT TIME ZONE 'UTC')::date ORDER BY day
			""", nativeQuery = true)
	List<Object[]> countItemsByDaySince(@Param("since") OffsetDateTime since);

}
