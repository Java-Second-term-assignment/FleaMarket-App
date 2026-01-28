package com.example.flea_market_app.transaction.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.transaction.domain.Order;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.domain.ReviewEntity;
import com.example.flea_market_app.transaction.domain.ReviewRating;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.transaction.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final OrderRepository orderRepository;
	private final ReviewRepository reviewRepository;

	@Transactional
	public void submitReview(UUID orderId, UUID currentUserId, ReviewRating rating, String comment) {
		OrderEntity orderEntity = orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.USER));

		Order order = new Order(
				orderEntity.getId(),
				orderEntity.getItemId(),
				orderEntity.getBuyerId(),
				orderEntity.getSellerId(),
				OrderStatus.valueOf(orderEntity.getStatus()));

		order.assertParticipant(currentUserId);

		// DB的にもレビューはいつでも入れられるが、業務ルールで COMPLETED 後に限定
		if (order.getStatus() != OrderStatus.COMPLETED) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_STATE,
					"error.review.order_not_completed");
		}

		// 1取引につき reviewer は1回（DB UNIQUE + アプリでも事前チェック）
		if (reviewRepository.existsByOrderIdAndReviewerId(orderId, currentUserId)) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_STATE, "You have already reviewed this order.");
		}

		UUID revieweeId = order.getBuyerId().equals(currentUserId)
				? order.getSellerId()
				: order.getBuyerId();

		ReviewEntity e = new ReviewEntity();
		e.setId(UUID.randomUUID());
		e.setOrderId(orderId);
		e.setReviewerId(currentUserId);
		e.setRevieweeId(revieweeId);
		e.setRating(rating.name());
		e.setComment(comment);
		e.setCreatedAt(OffsetDateTime.now());

		reviewRepository.save(e);

		// 将来:
		// - 両者レビューが揃ったら user_ranks 反映 or 集計
		//   （ただし transaction が user を直接更新しないのが疎結合で安全）
		// - notification を event 経由で投げる
	}
}
