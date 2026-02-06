package com.example.flea_market_app.catalog.service;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemViewEntity;
import com.example.flea_market_app.catalog.repository.ItemViewRepository;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

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
		List<?> content = itemViewRepository.findTopViewedItemIds(status, PageRequest.of(0, limit))
				.getContent();
		return content.stream()
				.map(row -> toUuid(row instanceof Object[] ? ((Object[]) row)[0] : row))
				.toList();
	}

	private static UUID toUuid(Object value) {
		if (value instanceof UUID u) {
			return u;
		}
		if (value instanceof byte[] bytes && bytes.length >= 16) {
			ByteBuffer bb = ByteBuffer.wrap(bytes);
			return new UUID(bb.getLong(), bb.getLong());
		}
		if (value instanceof String s) {
			return UUID.fromString(s);
		}
		throw new ValidationBusinessException(ErrorCode.INVALID_ID, ErrorCode.INVALID_ID.getMessageKey());
	}
}
