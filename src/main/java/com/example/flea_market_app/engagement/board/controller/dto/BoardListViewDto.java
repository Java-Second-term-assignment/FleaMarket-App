package com.example.flea_market_app.engagement.board.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardListViewDto {

	private UUID id;
	private String title;
	private String createdAt;
	private String category;
	private long replyCount;
}
