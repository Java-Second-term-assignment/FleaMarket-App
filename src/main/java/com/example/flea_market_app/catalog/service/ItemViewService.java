package com.example.flea_market_app.catalog.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemViewEntity;
import com.example.flea_market_app.catalog.repository.ItemViewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemViewService {

	private final ItemViewRepository itemViewRepository;

	@Transactional
	public void recordViewIfNew(UUID userId, UUID itemId) {
		if (itemViewRepository.existsByUserIdAndItemId(userId, itemId)) {
			return;
		}
		ItemViewEntity entity = new ItemViewEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(userId);
		entity.setItemId(itemId);
		entity.setViewedAt(OffsetDateTime.now());
		itemViewRepository.save(entity);
	}

	@Transactional(readOnly = true)
	public List<UUID> findTopViewedItemIds(String status, int limit) {
		return itemViewRepository.findTopViewedItemIds(status, PageRequest.of(0, limit))
				.getContent()
				.stream()
				.map(row -> (UUID) row[0])
				.toList();
	}
}
