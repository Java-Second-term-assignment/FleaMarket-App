package com.example.flea_market_app.user.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.common.constant.ImageConstants;
import com.example.flea_market_app.user.controller.dto.UserProductItemDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProductsQueryService {

	private static final String STATUS_DELETED = "DELETED";
	private static final int MAX_ITEMS = 50;

	private final ItemRepository itemRepository;
	private final ItemImageService itemImageService;

	/**
	 * 指定ユーザーが出品した商品一覧を取得する（DELETED 除外、最大50件、更新日時降順）。
	 */
	@Transactional(readOnly = true)
	public List<UserProductItemDto> listBySeller(UUID sellerId) {
		List<ItemEntity> items = itemRepository.findBySellerIdAndStatusNotOrderByUpdatedAtDesc(sellerId, STATUS_DELETED);
		return items.stream()
				.limit(MAX_ITEMS)
				.map(this::toDto)
				.toList();
	}

	private UserProductItemDto toDto(ItemEntity item) {
		String imageUrl = itemImageService.getThumbnailImageUrl(item.getId());
		if (imageUrl == null || imageUrl.isBlank()) {
			imageUrl = ImageConstants.NO_IMAGE_PATH;
		}
		return new UserProductItemDto(
				item.getId(),
				imageUrl,
				item.getName() != null ? item.getName() : "");
	}
}
