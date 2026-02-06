package com.example.flea_market_app.catalog.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.constant.ImageConstants;
import com.example.flea_market_app.catalog.controller.dto.CategoryOptionDto;
import com.example.flea_market_app.catalog.controller.dto.ProductDetailViewDto;
import com.example.flea_market_app.catalog.controller.dto.ProductListViewDto;
import com.example.flea_market_app.catalog.controller.dto.RankingItemDto;
import com.example.flea_market_app.catalog.domain.CategoryEntity;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.CategoryRepository;
import com.example.flea_market_app.catalog.repository.ItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductListService {

	private static final String STATUS_PUBLISHED = "PUBLISHED";
	private static final String STATUS_SOLD = "SOLD";
	private static final int DEFAULT_PAGE_SIZE = 50;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int RANKING_SIZE = 5;

	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;
	private final ItemImageService itemImageService;
	private final ItemViewService itemViewService;

	@Transactional(readOnly = true)
	public ProductListResult getProducts(
			Optional<String> keyword,
			Optional<UUID> categoryId,
			String sort,
			int page,
			int pageSize) {
		int safePage = Math.max(0, page);
		int safeSize = Math.min(MAX_PAGE_SIZE, Math.max(1, pageSize));
		PageRequest pageRequest = createPageRequest(sort, safePage, safeSize);
		Page<ItemEntity> itemPage;

		if (keyword.isPresent() && !keyword.get().isBlank()) {
			String kw = keyword.get().trim();
			if (categoryId.isPresent()) {
				itemPage = itemRepository.findByStatusAndKeywordAndCategoryId(STATUS_PUBLISHED, kw, categoryId.get(), pageRequest);
			} else {
				itemPage = itemRepository.findByStatusAndKeyword(STATUS_PUBLISHED, kw, pageRequest);
			}
		} else if (categoryId.isPresent()) {
			itemPage = itemRepository.findByStatusAndCategoryId(STATUS_PUBLISHED, categoryId.get(), pageRequest);
		} else {
			itemPage = itemRepository.findByStatus(STATUS_PUBLISHED, pageRequest);
		}

		List<ProductListViewDto> products = toProductListDtos(itemPage.getContent());
		return new ProductListResult(
				products,
				itemPage.getTotalPages(),
				itemPage.getNumber(),
				itemPage.getSize());
	}

	public record ProductListResult(
			List<ProductListViewDto> products,
			int totalPages,
			int currentPage,
			int pageSize) {
	}

	@Transactional(readOnly = true)
	public List<CategoryOptionDto> getCategories() {
		return categoryRepository.findActiveLeafCategories().stream()
				.map(c -> new CategoryOptionDto(c.getId(), c.getName()))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<RankingItemDto> getRankings() {
		List<UUID> viewedIds = itemViewService.findTopViewedItemIds(STATUS_PUBLISHED, RANKING_SIZE);
		List<UUID> orderedIds = new ArrayList<>(viewedIds);
		if (orderedIds.size() < RANKING_SIZE) {
			Set<UUID> viewedSet = Set.copyOf(viewedIds);
			List<ItemEntity> fallback = itemRepository.findByStatus(STATUS_PUBLISHED,
					PageRequest.of(0, RANKING_SIZE * 2, Sort.by("createdAt").descending())).getContent();
			fallback.stream()
					.map(ItemEntity::getId)
					.filter(id -> !viewedSet.contains(id))
					.limit(RANKING_SIZE - orderedIds.size())
					.forEach(orderedIds::add);
		}
		if (orderedIds.isEmpty()) {
			return List.of();
		}
		Map<UUID, ItemEntity> byId = itemRepository.findAllById(orderedIds).stream()
				.collect(Collectors.toMap(ItemEntity::getId, e -> e));
		List<ItemEntity> items = orderedIds.stream()
				.map(byId::get)
				.filter(Objects::nonNull)
				.toList();
		return items.stream()
				.map(this::toRankingDto)
				.toList();
	}

	private PageRequest createPageRequest(String sort, int page, int pageSize) {
		Sort order = switch (sort != null ? sort : "new") {
			case "low_price" -> Sort.by("priceAmount").ascending();
			case "popular", "new" -> Sort.by("createdAt").descending();
			default -> Sort.by("createdAt").descending();
		};
		return PageRequest.of(page, pageSize, order);
	}

	private List<ProductListViewDto> toProductListDtos(List<ItemEntity> items) {
		if (items.isEmpty()) {
			return List.of();
		}
		Set<UUID> categoryIds = items.stream().map(ItemEntity::getCategoryId).collect(Collectors.toSet());
		Map<UUID, String> categoryNames = categoryRepository.findAllById(categoryIds).stream()
				.collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));

		return items.stream()
				.map(item -> toProductListViewDto(item, categoryNames.getOrDefault(item.getCategoryId(), "")))
				.toList();
	}

	private ProductListViewDto toProductListViewDto(ItemEntity item, String categoryName) {
		String imageUrl = itemImageService.getThumbnailImageUrl(item.getId());
		if (imageUrl == null) {
			imageUrl = ImageConstants.NO_IMAGE_PATH;
		}
		return new ProductListViewDto(
				item.getId(),
				item.getName(),
				formatPrice(item.getPriceAmount()),
				imageUrl,
				categoryName,
				STATUS_SOLD.equals(item.getStatus()));
	}

	private RankingItemDto toRankingDto(ItemEntity item) {
		String imageUrl = itemImageService.getThumbnailImageUrl(item.getId());
		if (imageUrl == null) {
			imageUrl = ImageConstants.NO_IMAGE_PATH;
		}
		return new RankingItemDto(item.getId(), imageUrl, item.getName(), formatPrice(item.getPriceAmount()));
	}

	private String formatPrice(Long amount) {
		return amount != null ? String.valueOf(amount) : "0";
	}

	@Transactional(readOnly = true)
	public List<ProductListViewDto> getRelatedProducts(UUID currentItemId, int limit) {
		return itemRepository.findById(currentItemId)
				.filter(item -> item.getCategoryId() != null)
				.map(item -> itemRepository.findByStatusAndCategoryIdAndIdNot(
						STATUS_PUBLISHED, item.getCategoryId(), currentItemId,
						PageRequest.of(0, limit, Sort.by("createdAt").descending())))
				.map(page -> toProductListDtos(page.getContent()))
				.orElse(List.of());
	}

	@Transactional(readOnly = true)
	public Optional<ProductDetailViewDto> getProductDetail(UUID itemId) {
		return itemRepository.findById(itemId)
				.filter(item -> STATUS_PUBLISHED.equals(item.getStatus()) || "SOLD".equals(item.getStatus()))
				.map(this::toProductDetailDto);
	}

	private ProductDetailViewDto toProductDetailDto(ItemEntity item) {
		List<String> imageUrls = itemImageService.getItemImageUrls(item.getId());
		String mainImageUrl = imageUrls.isEmpty() ? ImageConstants.NO_IMAGE_PATH : imageUrls.get(0);
		List<ProductDetailViewDto.ProductImageDto> images = imageUrls.stream()
				.map(url -> new ProductDetailViewDto.ProductImageDto(url, url))
				.toList();

		String categoryName = categoryRepository.findById(item.getCategoryId())
				.map(CategoryEntity::getName)
				.orElse("");

		int stock = STATUS_PUBLISHED.equals(item.getStatus()) ? 1 : 0;

		return ProductDetailViewDto.builder()
				.id(item.getId())
				.name(item.getName())
				.description(item.getDescription())
				.price(item.getPriceAmount())
				.mainImageUrl(mainImageUrl)
				.images(images)
				.category(new ProductDetailViewDto.CategoryDisplayDto(categoryName))
				.stock(stock)
				.build();
	}
}
