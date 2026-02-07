package com.example.flea_market_app.catalog.service;

import java.util.Comparator;
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
	 * 商品に紐づく全COMPLETED注文のレビューを取得し、作成日時順で返す。
	 */
	@Transactional(readOnly = true)
	public List<ProductReviewViewDto> getReviewsForItem(UUID itemId) {
		List<ReviewEntity> allReviews = collectReviewsForItem(itemId);
		return allReviews.stream()
				.sorted(Comparator.comparing(ReviewEntity::getCreatedAt))
				.map(this::toViewDto)
				.toList();
	}

	/**
	 * 商品のレビューサマリ（件数・平均・表示用スター）を取得する。
	 * その商品に紐づく全COMPLETED注文のレビューを集計する。
	 */
	@Transactional(readOnly = true)
	public ReviewSummaryDto getReviewSummary(UUID itemId) {
		List<ReviewEntity> reviews = collectReviewsForItem(itemId);

		if (reviews.isEmpty()) {
			return new ReviewSummaryDto(0, 0.0, toStarsDisplay(0.0));
		}

		long goodCount = reviews.stream().filter(r -> "GOOD".equals(r.getRating())).count();
		long badCount = reviews.size() - goodCount;
		double average = (5.0 * goodCount + 1.0 * badCount) / reviews.size();
		String stars = toStarsDisplay(average);

		return new ReviewSummaryDto(reviews.size(), average, stars);
	}

	private List<ReviewEntity> collectReviewsForItem(UUID itemId) {
		return orderRepository.findByItemIdAndStatusOrderByCreatedAtDesc(itemId, "COMPLETED")
				.stream()
				.flatMap(order -> reviewRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()).stream())
				.toList();
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
