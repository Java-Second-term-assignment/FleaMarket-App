package com.example.flea_market_app.catalog.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.catalog.service.dto.ItemSummary;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemQueryServiceImpl implements ItemQueryService {

	private static final String PUBLISHED = "PUBLISHED";

	private final ItemRepository itemRepository;

	@Override
	@Transactional(readOnly = true)
	public void assertExists(UUID itemId) {
		if (!itemRepository.existsById(itemId)) {
			throw NotFoundBusinessException.of(ResourceType.ITEM);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public void assertExistsAndPublished(UUID itemId) {
		ItemEntity item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));
		if (!PUBLISHED.equals(item.getStatus())) {
			throw new ValidationBusinessException(
					ErrorCode.ITEM_NOT_PUBLISHED,
					ErrorCode.ITEM_NOT_PUBLISHED.getMessageKey());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<ItemSummary> findItemSummary(UUID itemId) {
		return itemRepository.findById(itemId).map(this::toSummary);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ItemSummary> findItemSummaries(Collection<UUID> itemIds) {
		if (itemIds == null || itemIds.isEmpty()) {
			return List.of();
		}
		List<ItemEntity> entities = itemRepository.findAllById(itemIds);
		return entities.stream().map(this::toSummary).collect(Collectors.toList());
	}

	private ItemSummary toSummary(ItemEntity e) {
		return new ItemSummary(
				e.getId(),
				e.getName(),
				e.getPriceAmount(),
				e.getCurrency(),
				e.getStatus());
	}
}
