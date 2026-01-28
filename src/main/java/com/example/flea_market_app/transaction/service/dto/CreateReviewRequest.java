package com.example.flea_market_app.transaction.service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.flea_market_app.transaction.domain.ReviewRating;

public class CreateReviewRequest {

	@NotNull
	private ReviewRating rating; // GOOD / BAD

	@Size(max = 2000)
	private String comment;

	public ReviewRating getRating() {
		return rating;
	}

	public void setRating(ReviewRating rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
