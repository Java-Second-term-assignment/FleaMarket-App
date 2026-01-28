package com.example.flea_market_app.transaction.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;

/**
 * DB: reviews
 * - UNIQUE(order_id, reviewer_id)
 * - rating: GOOD/BAD
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
		@UniqueConstraint(name = "uq_reviews_order_reviewer", columnNames = { "order_id", "reviewer_id" })
})
@Getter
@Setter
public class ReviewEntity {

	@Id
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "order_id", nullable = false, columnDefinition = "uuid")
	private UUID orderId;

	@Column(name = "reviewer_id", nullable = false, columnDefinition = "uuid")
	private UUID reviewerId;

	@Column(name = "reviewee_id", nullable = false, columnDefinition = "uuid")
	private UUID revieweeId;

	@Column(nullable = false)
	private String rating;

	@Column(length = 2000)
	private String comment;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
