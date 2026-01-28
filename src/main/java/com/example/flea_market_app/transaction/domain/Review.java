package com.example.flea_market_app.transaction.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Review {

	private final UUID id;
	private final UUID orderId;
	private final UUID reviewerId;
	private final UUID revieweeId;

	private final ReviewRating rating;
	private final String comment;
	private final OffsetDateTime createdAt;

	public Review(UUID id, UUID orderId, UUID reviewerId, UUID revieweeId,
			ReviewRating rating, String comment, OffsetDateTime createdAt) {

		this.id = Objects.requireNonNull(id);
		this.orderId = Objects.requireNonNull(orderId);
		this.reviewerId = Objects.requireNonNull(reviewerId);
		this.revieweeId = Objects.requireNonNull(revieweeId);
		this.rating = Objects.requireNonNull(rating);
		this.comment = comment;
		this.createdAt = Objects.requireNonNull(createdAt);
	}

}
