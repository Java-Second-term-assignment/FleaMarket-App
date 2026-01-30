package com.example.flea_market_app.engagement.favorite.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.catalog.service.dto.ItemSummary;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.engagement.favorite.domain.FavoriteEntity;
import com.example.flea_market_app.engagement.favorite.repository.FavoriteRepository;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

	private final FavoriteRepository favoriteRepository;
	private final ItemQueryService itemQueryService;
	private final ItemImageService itemImageService;

	@Override
	@Transactional
	public void add(UUID userId, UUID itemId) {
		itemQueryService.assertExistsAndPublished(itemId);

		if (favoriteRepository.existsByUserIdAndItemId(userId, itemId)) {
			throw new ValidationBusinessException(ErrorCode.ALREADY_FAVORITED, ErrorCode.ALREADY_FAVORITED.getMessageKey());
		}

		FavoriteEntity entity = new FavoriteEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(userId);
		entity.setItemId(itemId);
		entity.setCreatedAt(OffsetDateTime.now());
		favoriteRepository.save(entity);
	}

	@Override
	@Transactional
	public void remove(UUID userId, UUID itemId) {
		favoriteRepository.deleteByUserIdAndItemId(userId, itemId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<FavoriteItemResponse> listByUser(UUID userId) {
		List<FavoriteEntity> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
		if (favorites.isEmpty()) {
			return List.of();
		}

		List<UUID> itemIds = favorites.stream().map(FavoriteEntity::getItemId).toList();
		List<ItemSummary> summaries = itemQueryService.findItemSummaries(itemIds);
		Map<UUID, ItemSummary> summaryByItemId = summaries.stream()
				.collect(Collectors.toMap(ItemSummary::getId, s -> s));

		List<FavoriteItemResponse> result = new ArrayList<>(favorites.size());
		for (FavoriteEntity fav : favorites) {
			ItemSummary summary = summaryByItemId.get(fav.getItemId());
			if (summary == null) {
				continue;
			}
			String thumbnailUrl = itemImageService.getThumbnailImageUrl(summary.getId());
			result.add(new FavoriteItemResponse(
					summary.getId(),
					summary.getName(),
					summary.getPriceAmount(),
					summary.getCurrency(),
					thumbnailUrl,
					summary.getStatus()));
		}
		return result;
	}
}
