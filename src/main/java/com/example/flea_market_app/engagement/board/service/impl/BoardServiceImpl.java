package com.example.flea_market_app.engagement.board.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.service.ItemQueryService;
import com.example.flea_market_app.engagement.board.domain.BoardPostEntity;
import com.example.flea_market_app.engagement.board.repository.BoardPostRepository;
import com.example.flea_market_app.engagement.board.service.BoardService;
import com.example.flea_market_app.engagement.board.service.dto.PostResponse;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

	private final BoardPostRepository boardPostRepository;
	private final ItemQueryService itemQueryService;
	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true)
	public List<PostResponse> getPosts(UUID itemId, int page, int size) {
		itemQueryService.assertExists(itemId);

		int safeSize = Math.max(1, Math.min(size, 100));
		List<BoardPostEntity> posts = boardPostRepository.findByItemIdOrderByCreatedAtDesc(
				itemId, PageRequest.of(page, safeSize));

		if (posts.isEmpty()) {
			return List.of();
		}

		Set<UUID> authorIds = posts.stream().map(BoardPostEntity::getAuthorId).collect(Collectors.toSet());
		Map<UUID, String> displayNamesByUserId = userRepository.findAllById(authorIds).stream()
				.collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName));

		List<PostResponse> result = new ArrayList<>(posts.size());
		for (BoardPostEntity post : posts) {
			String displayName = displayNamesByUserId.getOrDefault(post.getAuthorId(), "");
			result.add(new PostResponse(
					post.getId(),
					post.getAuthorId(),
					displayName,
					post.getContent(),
					post.getCreatedAt()));
		}
		return result;
	}

	@Override
	@Transactional
	public UUID createPost(UUID itemId, UUID authorId, String content) {
		itemQueryService.assertExistsAndPublished(itemId);

		BoardPostEntity entity = new BoardPostEntity();
		entity.setId(UUID.randomUUID());
		entity.setItemId(itemId);
		entity.setAuthorId(authorId);
		entity.setContent(content);
		entity.setCreatedAt(OffsetDateTime.now());
		boardPostRepository.save(entity);
		return entity.getId();
	}
}
