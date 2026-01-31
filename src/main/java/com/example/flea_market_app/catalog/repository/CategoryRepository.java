package com.example.flea_market_app.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.flea_market_app.catalog.domain.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

	@Query("SELECT c FROM CategoryEntity c WHERE c.active = true AND c.parentId IS NOT NULL ORDER BY c.sortOrder, c.name")
	List<CategoryEntity> findActiveLeafCategories();
}
