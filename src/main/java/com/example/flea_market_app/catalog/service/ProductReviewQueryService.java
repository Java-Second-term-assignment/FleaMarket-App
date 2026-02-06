package com.example.flea_market_app.catalog.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.transaction.domain.ReviewEntity;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.transaction.repository.ReviewRepository;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductReviewQueryService {

	private final OrderRepository orderRepository;
	private final ReviewRepository reviewRepository;
	private final UserRepository userRepository;

	/**
	 * 商品に紐づく注文のレビュー一覧を取得する。
	 */
	@Transactional(readOnly = true)
	public List<ProductReviewViewDto> getReviewsForItem(UUID itemId) {
		return orderRepository.findByItemIdAndStatus(itemId, "COMPLETED")
				.map(order -> reviewRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()))
				.orElse(Collections.emptyList())
				.stream()
				.map(this::toViewDto)
				.toList();
	}

	/**
	 * 商品のレビューサマリ（件数・平均・表示用スター）を取得する。
	 */
	@Transactional(readOnly = true)
	public ReviewSummaryDto getReviewSummary(UUID itemId) {
		List<ReviewEntity> reviews = orderRepository.findByItemIdAndStatus(itemId, "COMPLETED")
				.map(order -> reviewRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()))
				.orElse(Collections.emptyList());

		if (reviews.isEmpty()) {
			return new ReviewSummaryDto(0, 0.0, "★★★★☆");
		}

		long goodCount = reviews.stream().filter(r -> "GOOD".equals(r.getRating())).count();
		long badCount = reviews.size() - goodCount;
		double average = goodCount > 0 ? (5.0 * goodCount + 1.0 * badCount) / reviews.size() : 0.0;
		String stars = toStarsDisplay(average);

		return new ReviewSummaryDto(reviews.size(), average, stars);
	}

	private ProductReviewViewDto toViewDto(ReviewEntity r) {
		String userName = userRepository.findById(r.getReviewerId())
				.map(u -> u.getDisplayName() != null && !u.getDisplayName().isBlank()
						? u.getDisplayName()
						: "ユーザー")
				.orElse("ユーザー");
		String stars = "GOOD".equals(r.getRating()) ? "★★★★★" : "★☆☆☆☆";
		return new ProductReviewViewDto(
				userName,
				r.getCreatedAt(),
				stars,
				r.getComment() != null ? r.getComment() : "");
	}

	private static String toStarsDisplay(double avg) {
		if (avg >= 4.5) return "★★★★★";
		if (avg >= 3.5) return "★★★★☆";
		if (avg >= 2.5) return "★★★☆☆";
		if (avg >= 1.5) return "★★☆☆☆";
		if (avg >= 0.5) return "★☆☆☆☆";
		return "☆☆☆☆☆";
	}

	public record ProductReviewViewDto(String userName, java.time.OffsetDateTime createdAt, String stars, String comment) {
	}

	public record ReviewSummaryDto(int count, double average, String stars) {
	}
}
