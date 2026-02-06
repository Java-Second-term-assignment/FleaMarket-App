package com.example.flea_market_app.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flea_market_app.admin.domain.ReportEntity;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {

	/**
	 * 指定ターゲット種別の通報を新しい順で取得（通報商品一覧用）。
	 */
	@Query("SELECT r FROM ReportEntity r WHERE r.targetType = :targetType ORDER BY r.createdAt DESC")
	List<ReportEntity> findByTargetTypeOrderByCreatedAtDesc(@Param("targetType") String targetType);
}
