package com.example.flea_market_app.engagement.board.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.engagement.board.service.BoardListService;
import com.example.flea_market_app.engagement.board.service.BoardService;
import com.example.flea_market_app.engagement.board.service.dto.PostResponse;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardPageController {

	private static final Logger log = LoggerFactory.getLogger(BoardPageController.class);
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	private final BoardListService boardListService;
	private final BoardService boardService;
	private final ItemQueryService itemQueryService;

	@GetMapping("/board")
	public String boardList(Model model) {
		model.addAttribute("boards", boardListService.getBoards());
		log.info("Board list displayed");
		return "board/board_list";
	}

	@GetMapping("/board/{id}")
	public String boardDetail(@PathVariable UUID id, Model model) {
		log.info("Board detail requested: boardId={}", id);
		itemQueryService.assertExists(id);

		List<PostResponse> posts = boardService.getPosts(id, 50);

		BoardDetailStub post;
		List<CommentStub> comments;
		if (posts.isEmpty()) {
			post = new BoardDetailStub(id, "掲示板", "（投稿はまだありません）");
			comments = List.of();
		} else {
			PostResponse first = posts.get(0);
			post = new BoardDetailStub(first.getId(), first.getContent(), first.getContent());
			comments = posts.stream()
					.map(p -> new CommentStub(
							p.getContent().length() > 30 ? p.getContent().substring(0, 30) + "..." : p.getContent(),
							p.getContent(),
							p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FORMAT) : "",
							null, 0, p.getAuthorDisplayName() != null ? p.getAuthorDisplayName() : "ユーザー", false))
					.toList();
		}

		model.addAttribute("post", post);
		model.addAttribute("comments", comments);
		return "board/board_detail";
	}

	private record BoardDetailStub(UUID id, String title, String content) {
	}

	private record CommentStub(String title, String body, String createdAt, Object parent, int replyCount, String userName, boolean editable) {
	}
}
