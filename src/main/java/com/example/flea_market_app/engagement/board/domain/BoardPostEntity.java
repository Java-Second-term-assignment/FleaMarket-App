package com.example.flea_market_app.engagement.board.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "board_posts")
@Getter
@Setter
public class BoardPostEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "item_id", nullable = false)
	private UUID itemId;

	@Column(name = "author_id", nullable = false)
	private UUID authorId;

	@Column(name = "content", nullable = false, columnDefinition = "text")
	private String content;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;
}
