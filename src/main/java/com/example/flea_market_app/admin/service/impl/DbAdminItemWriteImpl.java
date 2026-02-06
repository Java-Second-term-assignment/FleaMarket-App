package com.example.flea_market_app.admin.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.service.port.AdminItemWritePort;
import com.example.flea_market_app.catalog.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

/**
 * 管理者による items の論理削除（status = DELETED）。
 *
 * 注意:
 * - 物理削除は行わない
 * - DB制約（items.status）に従う
 * - 戻り値は「更新されたかどうか」のみ
 */
@Component
@RequiredArgsConstructor
public class DbAdminItemWriteImpl implements AdminItemWritePort {

	private final ItemRepository itemRepository;

	@Override
	@Transactional
	public boolean markDeleted(UUID itemId) {
		return itemRepository.updateStatus(itemId, "DELETED") > 0;
	}

	@Override
	@Transactional
	public boolean markSuspended(UUID itemId) {
		return itemRepository.updateStatus(itemId, "SUSPENDED") > 0;
	}

	@Override
	@Transactional
	public boolean restoreFromDeleted(UUID itemId) {
		return itemRepository.updateStatus(itemId, "PUBLISHED") > 0;
	}

	@Override
	@Transactional
	public boolean restoreToPublished(UUID itemId) {
		return itemRepository.updateStatus(itemId, "PUBLISHED") > 0;
	}

	@Override
	@Transactional
	public boolean updateNameAndPrice(UUID itemId, String name, Long priceAmount) {
		return itemRepository.updateNameAndPrice(itemId, name, priceAmount) > 0;
	}

	@Override
	@Transactional
	public boolean deletePermanently(UUID itemId) {
		if (!itemRepository.existsById(itemId))
			return false;
		itemRepository.deleteById(itemId);
		return true;
	}
}
