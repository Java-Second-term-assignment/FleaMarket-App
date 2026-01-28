package com.example.flea_market_app.listing.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.service.ItemImageService;
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

	private final ItemRepository itemRepository;
	private final ItemImageService itemImageService;

	@Override
	@Transactional
	public CreateItemResponse createItem(UUID sellerId, CreateItemRequest request, List<MultipartFile> images) {
		log.info("Creating item for seller: {}, name: {}", sellerId, request.getName());

		// 商品エンティティの作成
		ItemEntity item = new ItemEntity();
		item.setId(UUID.randomUUID());
		item.setSellerId(sellerId);
		item.setCategoryId(request.getCategoryId());
		item.setName(request.getName());
		item.setDescription(request.getDescription() != null ? request.getDescription() : "");
		item.setPriceAmount(request.getPriceAmount());
		item.setCurrency("JPY");
		item.setStatus("DRAFT");
		item.setCondition(request.getCondition());
		item.setShippingFeePayer(request.getShippingFeePayer());

		// 商品を保存
		item = itemRepository.save(item);
		log.info("Successfully created item: {}", item.getId());

		// 画像のアップロード（画像がある場合のみ）
		List<String> imageUrls = new ArrayList<>();
		if (images != null && !images.isEmpty()) {
			try {
				itemImageService.uploadItemImages(item.getId(), images);
				imageUrls = itemImageService.getItemImageUrls(item.getId());
				log.info("Successfully uploaded {} images for item: {}", imageUrls.size(), item.getId());
			} catch (Exception e) {
				log.error("Failed to upload images for item: {}", item.getId(), e);
				// 画像アップロード失敗時は商品も削除（トランザクションロールバック）
				throw e;
			}
		}

		log.info("Successfully created item with {} images: {}", imageUrls.size(), item.getId());
		return new CreateItemResponse(item.getId(), imageUrls);
	}
}
