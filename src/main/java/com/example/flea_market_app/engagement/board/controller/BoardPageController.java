package com.example.flea_market_app.engagement.board.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.catalog.service.ProductListService;
import com.example.flea_market_app.engagement.board.service.BoardListService;
import com.example.flea_market_app.engagement.board.service.BoardService;
import com.example.flea_market_app.engagement.board.service.dto.PostResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@Validated
public class BoardPageController {

	private static final Logger log = LoggerFactory.getLogger(BoardPageController.class);
	private static final int DEFAULT_POST_LIMIT = 50;

	private final BoardListService boardListService;
	private final BoardService boardService;
	private final ItemQueryService itemQueryService;
	private final ProductListService productListService;

	@GetMapping("/board")
	public String boardList(Model model) {
		model.addAttribute("boards", boardListService.getBoards());
		model.addAttribute("categories", productListService.getCategories());
		model.addAttribute("products", productListService.getProducts(
				Optional.empty(), Optional.empty(), "new", 0, 100).products());
		log.info("Board list displayed");
		return "board/board_list";
	}

	@GetMapping("/board/{boardId}")
	public String boardDetail(@PathVariable UUID boardId, Model model) {
		log.info("Board detail requested: boardId={}", boardId);
		itemQueryService.assertExists(boardId);

		List<PostResponse> posts = boardService.getPosts(boardId, 0, DEFAULT_POST_LIMIT);

		model.addAttribute("boardId", boardId);
		model.addAttribute("posts", posts);
		return "board/board_detail";
	}

	@PostMapping("/board/{boardId}/posts")
	public String createPost(
			@PathVariable UUID boardId,
			@RequestParam @NotBlank(message = "本文を入力してください") @Size(max = 1000, message = "本文は1000文字以内で入力してください") String content) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("Creating post: boardId={}, userId={}", boardId, userId);

		boardService.createPost(boardId, userId, content);
		log.info("Created post for board: {}", boardId);
		return "redirect:/board/" + boardId;
	}
}
