package com.example.flea_market_app.listing.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 出品者本人による出品停止・削除を行うサービス。
 */
@Service
@RequiredArgsConstructor
public class SellerListingService {

	private static final String STATUS_PUBLISHED = "PUBLISHED";
	private static final String STATUS_SUSPENDED = "SUSPENDED";

	private final ItemRepository itemRepository;

	/**
	 * 出品停止。PUBLISHED の商品のみ実行可能。出品者本人のみ。
	 */
	@Transactional
	public void suspend(UUID userId, UUID itemId) {
		ItemEntity item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (!item.getSellerId().equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
		if (!STATUS_PUBLISHED.equals(item.getStatus())) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_STATE,
					"error.invalid_state");
		}
		itemRepository.updateStatus(itemId, STATUS_SUSPENDED);
	}

	/**
	 * 出品再開。SUSPENDED の商品のみ実行可能。出品者本人のみ。
	 */
	@Transactional
	public void unsuspend(UUID userId, UUID itemId) {
		ItemEntity item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (!item.getSellerId().equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
		if (!STATUS_SUSPENDED.equals(item.getStatus())) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_STATE,
					"error.invalid_state");
		}
		itemRepository.updateStatus(itemId, STATUS_PUBLISHED);
	}

	/**
	 * 削除（論理削除）。SUSPENDED の商品のみ実行可能。出品者本人のみ。
	 */
	@Transactional
	public void delete(UUID userId, UUID itemId) {
		ItemEntity item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (!item.getSellerId().equals(userId)) {
			throw new AccessDeniedBusinessException();
		}
		if (!STATUS_SUSPENDED.equals(item.getStatus())) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_STATE,
					"error.invalid_state");
		}
		itemRepository.updateStatus(itemId, "DELETED");
	}
}
