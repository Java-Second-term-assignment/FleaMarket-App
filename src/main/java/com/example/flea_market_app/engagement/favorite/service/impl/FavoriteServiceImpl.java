package com.example.flea_market_app.engagement.favorite.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.engagement.favorite.domain.FavoriteEntity;
import com.example.flea_market_app.engagement.favorite.repository.FavoriteRepository;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

	private static final String PUBLISHED = "PUBLISHED";

	private final FavoriteRepository favoriteRepository;
	private final ItemRepository itemRepository;
	private final ItemImageService itemImageService;

	@Override
	@Transactional
	public void add(UUID userId, UUID itemId) {
		ItemEntity item = itemRepository.findById(itemId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ITEM));

		if (!PUBLISHED.equals(item.getStatus())) {
			throw new ValidationBusinessException(ErrorCode.ITEM_NOT_PUBLISHED, ErrorCode.ITEM_NOT_PUBLISHED.getMessageKey());
		}

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
		List<FavoriteItemResponse> result = new ArrayList<>(favorites.size());

		for (FavoriteEntity fav : favorites) {
			itemRepository.findById(fav.getItemId()).ifPresent(item -> {
				String thumbnailUrl = itemImageService.getThumbnailImageUrl(item.getId());
				result.add(new FavoriteItemResponse(
						item.getId(),
						item.getName(),
						item.getPriceAmount(),
						item.getCurrency(),
						thumbnailUrl,
						item.getStatus()));
			});
		}

		return result;
	}
}
