package com.example.flea_market_app.engagement.board.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.CategoryEntity;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.CategoryRepository;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.engagement.board.controller.dto.BoardListViewDto;
import com.example.flea_market_app.engagement.board.repository.BoardPostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardListService {

	private static final String STATUS_PUBLISHED = "PUBLISHED";
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	private final ItemRepository itemRepository;
	private final CategoryRepository categoryRepository;
	private final BoardPostRepository boardPostRepository;

	@Transactional(readOnly = true)
	public List<BoardListViewDto> getBoards() {
		List<ItemEntity> items = itemRepository.findByStatus(
				STATUS_PUBLISHED, PageRequest.of(0, 100, Sort.by("createdAt").descending())).getContent();

		if (items.isEmpty()) {
			return List.of();
		}

		Map<UUID, String> categoryNames = categoryRepository.findAllById(
						items.stream().map(ItemEntity::getCategoryId).collect(Collectors.toSet()))
				.stream()
				.collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));

		return items.stream()
				.map(item -> new BoardListViewDto(
						item.getId(),
						item.getName(),
						item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_FORMAT) : "",
						categoryNames.getOrDefault(item.getCategoryId(), ""),
						boardPostRepository.countByItemId(item.getId())))
				.toList();
	}
}
