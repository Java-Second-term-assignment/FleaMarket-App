package com.example.flea_market_app.admin.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.admin.domain.ItemModerationEntity;

public interface ItemModerationRepository extends JpaRepository<ItemModerationEntity, UUID> {

	/**
	 * 違反と判定された記録を新しい順で取得（管理者違反リスト用）。
	 */
	List<ItemModerationEntity> findByRejectedTrueOrderByCreatedAtDesc();
}
