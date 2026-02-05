package com.example.flea_market_app.transaction.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.service.ItemImageService;
import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.catalog.service.dto.ItemSummary;
import com.example.flea_market_app.common.constant.ImageConstants;
import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.transaction.repository.ReviewRepository;
import com.example.flea_market_app.transaction.service.dto.OrderDetailDto;
import com.example.flea_market_app.transaction.service.dto.OrderListItemDto;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 注文一覧・詳細の参照専用サービス。
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {

	private final OrderRepository orderRepository;
	private final ItemQueryService itemQueryService;
	private final ItemImageService itemImageService;
	private final UserRepository userRepository;
	private final ReviewRepository reviewRepository;

	@Transactional(readOnly = true)
	public List<OrderListItemDto> getMyOrders(UUID userId, String scope) {
		List<OrderEntity> entities = "seller".equalsIgnoreCase(scope)
				? orderRepository.findBySellerIdOrderByUpdatedAtDesc(userId)
				: orderRepository.findByBuyerIdOrderByUpdatedAtDesc(userId);
		String role = "seller".equalsIgnoreCase(scope) ? "SELLER" : "BUYER";

		if (entities.isEmpty()) {
			return List.of();
		}

		List<UUID> itemIds = entities.stream().map(OrderEntity::getItemId).distinct().toList();
		Map<UUID, ItemSummary> summaries = itemQueryService.findItemSummaries(itemIds).stream()
				.collect(Collectors.toMap(ItemSummary::getId, s -> s));

		return entities.stream()
				.map(e -> toListItemDto(e, role, summaries.get(e.getItemId())))
				.toList();
	}

	private OrderListItemDto toListItemDto(OrderEntity e, String role, ItemSummary summary) {
		String itemName = summary != null ? summary.getName() : "(不明)";
		String thumbnailUrl = e.getItemId() != null ? itemImageService.getThumbnailImageUrl(e.getItemId()) : null;
		if (thumbnailUrl == null) {
			thumbnailUrl = ImageConstants.NO_IMAGE_PATH;
		}
		return OrderListItemDto.builder()
				.orderId(e.getId())
				.itemId(e.getItemId())
				.itemName(itemName)
				.itemThumbnailUrl(thumbnailUrl)
				.status(e.getStatus())
				.totalAmount(e.getTotalAmount())
				.createdAt(e.getCreatedAt())
				.role(role)
				.build();
	}

	@Transactional(readOnly = true)
	public OrderDetailDto getOrderDetail(UUID orderId, UUID userId) {
		OrderEntity e = orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));

		UUID buyerId = e.getBuyerId();
		UUID sellerId = e.getSellerId();
		if (buyerId == null || sellerId == null) {
			throw new IllegalStateException("Order has null buyer or seller: orderId=" + e.getId());
		}
		boolean isBuyer = buyerId.equals(userId);
		boolean isSeller = sellerId.equals(userId);
		if (!isBuyer && !isSeller) {
			throw new AccessDeniedBusinessException();
		}

		String role = isBuyer ? "BUYER" : "SELLER";
		UUID counterpartyId = isBuyer ? sellerId : buyerId;
		String counterpartyDisplayName = userRepository.findById(counterpartyId)
				.map(UserEntity::getDisplayName)
				.orElse("");

		ItemSummary summary = itemQueryService.findItemSummary(e.getItemId()).orElse(null);
		String itemName = summary != null ? summary.getName() : "(不明)";
		String thumbnailUrl = itemImageService.getThumbnailImageUrl(e.getItemId());
		if (thumbnailUrl == null) {
			thumbnailUrl = ImageConstants.NO_IMAGE_PATH;
		}

		String statusStr = e.getStatus();
		if (statusStr == null) {
			throw new IllegalStateException("Order status is null: orderId=" + e.getId());
		}
		OrderStatus status = OrderStatus.valueOf(statusStr);
		boolean canConfirm = isBuyer && status == OrderStatus.PAID;
		boolean canShip = isSeller && status == OrderStatus.AWAITING_SHIPMENT;
		boolean canReceipt = isBuyer && status == OrderStatus.SHIPPED;
		boolean canCancel = (isBuyer || isSeller)
				&& status != OrderStatus.SHIPPED
				&& status != OrderStatus.COMPLETED
				&& status != OrderStatus.CANCELLED;
		boolean canReview = (isBuyer || isSeller)
				&& status == OrderStatus.COMPLETED
				&& !reviewRepository.existsByOrderIdAndReviewerId(orderId, userId);

		return OrderDetailDto.builder()
				.orderId(e.getId())
				.itemId(e.getItemId())
				.itemName(itemName)
				.itemThumbnailUrl(thumbnailUrl)
				.status(e.getStatus())
				.totalAmount(e.getTotalAmount())
				.createdAt(e.getCreatedAt())
				.role(role)
				.shippingAddressSnapshot(e.getShippingAddressSnapshot())
				.counterpartyDisplayName(counterpartyDisplayName)
				.canConfirm(canConfirm)
				.canShip(canShip)
				.canReceipt(canReceipt)
				.canCancel(canCancel)
				.canReview(canReview)
				.build();
	}
}
