package com.example.flea_market_app.engagement.board.service.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

	private UUID id;
	private UUID authorId;
	private String authorDisplayName;
	private String content;
	private OffsetDateTime createdAt;
}
