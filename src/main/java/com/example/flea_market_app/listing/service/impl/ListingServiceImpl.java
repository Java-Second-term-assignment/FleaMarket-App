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

	private final ItemRepository itemRepository;
	private final ItemImageService itemImageService;

	@Override
	@Transactional
	public CreateItemResponse createItem(UUID sellerId, CreateItemRequest request, List<MultipartFile> images) {
		log.info("Creating item for seller: {}, name: {}", sellerId, request.getName());

		// リクエストの condition / shippingFeePayer を Enum に変換（事前バリデーション済み想定）
		ItemCondition condition = ItemCondition.valueOf(request.getCondition());
		ShippingFeePayer shippingFeePayer = ShippingFeePayer.valueOf(request.getShippingFeePayer());

		// ドメインオブジェクトを生成し、Entity に変換して保存
		Listing listing = Listing.createDraft(
				sellerId,
				request.getCategoryId(),
				request.getName(),
				request.getDescription(),
				request.getPriceAmount(),
				condition,
				shippingFeePayer);
		ItemEntity item = listing.toEntity();
		item = itemRepository.save(item);
		log.info("Successfully created item: {}", item.getId());

		// 画像のアップロード（最低1枚必須）
		List<String> imageUrls = new ArrayList<>();
		try {
			itemImageService.uploadItemImages(item.getId(), images);
			imageUrls = itemImageService.getItemImageUrls(item.getId());
			log.info("Successfully uploaded {} images for item: {}", imageUrls.size(), item.getId());
		} catch (Exception e) {
			log.error("Failed to upload images for item: {}", item.getId(), e);
			// 画像アップロード失敗時は商品も削除（トランザクションロールバック）
			throw e;
		}

		log.info("Successfully created item with {} images: {}", imageUrls.size(), item.getId());
		return new CreateItemResponse(item.getId(), imageUrls);
	}
}
