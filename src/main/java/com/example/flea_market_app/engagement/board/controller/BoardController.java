package com.example.flea_market_app.engagement.board.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.common.response.ApiResponse;
import com.example.flea_market_app.engagement.board.service.BoardService;
import com.example.flea_market_app.engagement.board.service.dto.CreateBoardPostRequest;
import com.example.flea_market_app.engagement.board.service.dto.PostResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community/boards")
@Validated
public class BoardController {

	private static final Logger log = LoggerFactory.getLogger(BoardController.class);
	private static final int DEFAULT_POST_LIMIT = 50;

	private final BoardService boardService;

	/**
	 * 掲示板の投稿一覧を取得します（最新順）。未ログインでも閲覧可能です。
	 *
	 * @param boardId 掲示板ID（商品ID = item_id）
	 * @param limit   取得件数上限（省略時は {@value #DEFAULT_POST_LIMIT} 件）
	 * @return 投稿一覧
	 */
	@GetMapping("/{boardId}/posts")
	public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(
			@PathVariable UUID boardId,
			@RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
		log.info("Getting posts for board: {}, limit={}", boardId, limit);

		List<PostResponse> posts = boardService.getPosts(boardId, limit);

		log.info("Found {} posts for board: {}", posts.size(), boardId);
		return ResponseEntity.ok(ApiResponse.success(posts));
	}

	/**
	 * 掲示板に投稿します。公開中（PUBLISHED）の商品の掲示板にのみ投稿可能です。認証必須。
	 *
	 * @param boardId 掲示板ID（商品ID = item_id）
	 * @param request 投稿内容（本文）
	 * @return 201 Created、Location: /community/boards/{boardId}/posts/{postId}
	 */
	@PostMapping("/{boardId}/posts")
	public ResponseEntity<Void> createPost(
			@PathVariable UUID boardId,
			@Valid @RequestBody CreateBoardPostRequest request) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Creating post: boardId={}, userId={}", boardId, userId);

		UUID postId = boardService.createPost(boardId, userId, request.getContent());

		URI location = URI.create("/community/boards/" + boardId + "/posts/" + postId);
		log.info("Created post: postId={}", postId);
		return ResponseEntity.created(location).build();
	}
}
