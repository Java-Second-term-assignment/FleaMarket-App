package com.example.flea_market_app.listing.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.catalog.service.ItemService;
import com.example.flea_market_app.listing.domain.ItemCondition;
import com.example.flea_market_app.listing.domain.Listing;
import com.example.flea_market_app.listing.domain.ShippingFeePayer;
import com.example.flea_market_app.listing.service.ListingService;
import com.example.flea_market_app.listing.service.dto.CreateItemRequest;
import com.example.flea_market_app.listing.service.dto.CreateItemResponse;

import lombok.RequiredArgsConstructor;

/**
 * 商品出品に関するサービスの実装クラス。
 *
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

	private static final Logger log = LoggerFactory.getLogger(ListingServiceImpl.class);

	private final ItemService itemService;
	private final ItemImageService itemImageService;

	@Override
	@Transactional
	public CreateItemResponse createItem(UUID sellerId, CreateItemRequest request, List<MultipartFile> images) {
		log.info("Creating item for seller: {}, name: {}", sellerId, request.getName());

		ItemCondition condition = ItemCondition.valueOf(request.getCondition());
		ShippingFeePayer shippingFeePayer = ShippingFeePayer.valueOf(request.getShippingFeePayer());

		Listing draft = Listing.createDraft(
				sellerId,
				request.getCategoryId(),
				request.getName(),
				request.getDescription(),
				request.getPriceAmount(),
				condition,
				shippingFeePayer);

		UUID itemId = itemService.createDraftItem(
				draft.getSellerId(),
				draft.getCategoryId(),
				draft.getName(),
				draft.getDescription(),
				draft.getPriceAmount(),
				draft.getCondition().name(),
				draft.getShippingFeePayer().name());

		log.info("Successfully created item: {}", itemId);

		List<String> imageUrls = new ArrayList<>();
		try {
			itemImageService.uploadItemImages(itemId, images);
			imageUrls = itemImageService.getItemImageUrls(itemId);
			log.info("Successfully uploaded {} images for item: {}", imageUrls.size(), itemId);
		} catch (Exception e) {
			log.error("Failed to upload images for item: {}", itemId, e);
			throw e;
		}

		log.info("Successfully created item with {} images: {}", imageUrls.size(), itemId);
		return new CreateItemResponse(itemId, imageUrls);
	}
}
