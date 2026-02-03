package com.example.flea_market_app.admin.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.admin.controller.dto.AdminDashboardStatsDto;
import com.example.flea_market_app.admin.controller.dto.DateCountDto;
import com.example.flea_market_app.admin.controller.dto.StatusCountDto;
import com.example.flea_market_app.admin.controller.dto.TopViewedItemDto;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.repository.ItemViewRepository;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 管理者統計画面用の集計データを取得する。
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

	private static final int DEFAULT_DAYS = 30;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

	private final ItemRepository itemRepository;
	private final ItemViewRepository itemViewRepository;
	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;

	public AdminDashboardStatsDto getDashboardStats() {
		long totalActiveItems = itemRepository.countByStatus("PUBLISHED");
		long totalUsers = userRepository.count();
		return new AdminDashboardStatsDto(totalActiveItems, totalUsers);
	}

	/** 商品ステータス別件数（円グラフ用） */
	public List<StatusCountDto> getItemStatusCounts() {
		List<Object[]> rows = itemRepository.countGroupByStatus();
		return rows.stream()
				.map(row -> {
					String status = (String) row[0];
					long count = ((Number) row[1]).longValue();
					return new StatusCountDto(status, toStatusLabel(status), count);
				})
				.toList();
	}

	/** 直近N日間の日別商品登録数（棒/折れ線用） */
	public List<DateCountDto> getItemCountsByDay(int days) {
		OffsetDateTime since = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days);
		List<Object[]> rows = itemRepository.countItemsByDaySince(since);
		Map<String, Long> byDate = rows.stream()
				.collect(Collectors.toMap(
						row -> formatDate(row[0]),
						row -> ((Number) row[1]).longValue(),
						(a, b) -> a));
		return fillDateRange(days, byDate);
	}

	/** 直近N日間の日別ユーザー登録数（折れ線用） */
	public List<DateCountDto> getUserSignupCountsByDay(int days) {
		OffsetDateTime since = OffsetDateTime.now(ZoneOffset.UTC).minusDays(days);
		List<Object[]> rows = authUserRepository.countByDaySince(since);
		Map<String, Long> byDate = rows.stream()
				.collect(Collectors.toMap(
						row -> formatDate(row[0]),
						row -> ((Number) row[1]).longValue(),
						(a, b) -> a));
		return fillDateRange(days, byDate);
	}

	/** 閲覧数トップ商品（横棒/表用） */
	public List<TopViewedItemDto> getTopViewedItems() {
		List<Object[]> rows = itemViewRepository.findTopViewedItemIdsWithCount();
		if (rows.isEmpty()) return List.of();
		List<UUID> ids = rows.stream()
				.map(row -> (UUID) row[0])
				.toList();
		Map<UUID, String> names = itemRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(ItemEntity::getId, ItemEntity::getName, (a, b) -> a));
		return rows.stream()
				.map(row -> {
					UUID itemId = (UUID) row[0];
					long count = ((Number) row[1]).longValue();
					String name = names.getOrDefault(itemId, "—");
					return new TopViewedItemDto(itemId, name, count);
				})
				.toList();
	}

	private static String toStatusLabel(String status) {
		if (status == null) return "—";
		return switch (status) {
			case "ACTIVE", "PUBLISHED" -> "公開中";
			case "DRAFT" -> "下書き";
			case "DELETED" -> "削除済";
			default -> status;
		};
	}

	private static String formatDate(Object value) {
		if (value == null) return "";
		if (value instanceof java.sql.Date d) return d.toLocalDate().format(DATE_FORMAT);
		if (value instanceof java.time.LocalDate ld) return ld.format(DATE_FORMAT);
		return value.toString();
	}

	private static List<DateCountDto> fillDateRange(int days, Map<String, Long> byDate) {
		LocalDate end = LocalDate.now();
		LocalDate start = end.minusDays(days - 1);
		List<DateCountDto> result = new ArrayList<>();
		for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
			String key = d.format(DATE_FORMAT);
			long count = byDate.getOrDefault(key, 0L);
			result.add(new DateCountDto(key, count));
		}
		return result;
	}

	public int getDefaultDays() {
		return DEFAULT_DAYS;
	}
}
