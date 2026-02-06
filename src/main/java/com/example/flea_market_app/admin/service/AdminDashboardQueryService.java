package com.example.flea_market_app.admin.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.admin.controller.dto.AdminDashboardStatsDto;
import com.example.flea_market_app.admin.controller.dto.AdminProductRowDto;
import com.example.flea_market_app.admin.controller.dto.AdminUserRowDto;
import com.example.flea_market_app.admin.domain.ItemModerationEntity;
import com.example.flea_market_app.admin.domain.ReportEntity;
import com.example.flea_market_app.admin.repository.ItemModerationRepository;
import com.example.flea_market_app.admin.repository.ReportRepository;
import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 管理者ダッシュボード表示用の統計・ユーザー一覧・商品一覧を取得する。
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardQueryService {

	private static final String STATUS_PUBLISHED = "PUBLISHED";
	private static final String STATUS_DELETED = "DELETED";

	private final UserRepository userRepository;
	private final AuthUserRepository authUserRepository;
	private final ItemRepository itemRepository;
	private final ItemModerationRepository itemModerationRepository;
	private final ReportRepository reportRepository;

	public AdminDashboardStatsDto getDashboardStats() {
		long totalActiveItems = itemRepository.countByStatus(STATUS_PUBLISHED);
		long totalUsers = userRepository.count();
		return new AdminDashboardStatsDto(totalActiveItems, totalUsers);
	}

	public List<AdminUserRowDto> getUsersForDashboard() {
		List<UserEntity> users = userRepository.findAll();
		List<AuthUserEntity> authUsers = authUserRepository.findAll();
		Map<java.util.UUID, AuthUserEntity> authByUserId = authUsers.stream()
				.collect(Collectors.toMap(AuthUserEntity::getUserId, a -> a, (a, b) -> a));

		return users.stream()
				.map(u -> {
					AuthUserEntity auth = authByUserId.get(u.getId());
					String email = auth != null ? auth.getEmail() : "—";
					boolean admin = auth != null && auth.isAdmin();
					return new AdminUserRowDto(u.getId(), u.getDisplayName(), email, u.isActive(), admin);
				})
				.toList();
	}

	public List<AdminProductRowDto> getProductsForDashboard() {
		List<ItemEntity> items = itemRepository.findAllByStatusNot(STATUS_DELETED);
		List<java.util.UUID> sellerIds = items.stream()
				.map(ItemEntity::getSellerId)
				.distinct()
				.toList();
		Map<java.util.UUID, String> sellerNames = userRepository.findAllById(sellerIds).stream()
				.collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName, (a, b) -> a));

		return items.stream()
				.map(item -> {
					String sellerName = sellerNames.getOrDefault(item.getSellerId(), "—");
					String statusLabel = toStatusLabel(item.getStatus());
					return new AdminProductRowDto(
							item.getId(),
							item.getName(),
							item.getPriceAmount() != null ? item.getPriceAmount() : 0L,
							sellerName,
							statusLabel);
				})
				.toList();
	}

	public List<AdminUserRowDto> getBlacklistedUsers() {
		List<UserEntity> users = userRepository.findByActiveFalse();
		List<AuthUserEntity> authUsers = authUserRepository.findAll();
		Map<java.util.UUID, AuthUserEntity> authByUserId = authUsers.stream()
				.collect(Collectors.toMap(AuthUserEntity::getUserId, a -> a, (a, b) -> a));

		return users.stream()
				.map(u -> {
					AuthUserEntity auth = authByUserId.get(u.getId());
					String email = auth != null ? auth.getEmail() : "—";
					boolean admin = auth != null && auth.isAdmin();
					return new AdminUserRowDto(u.getId(), u.getDisplayName(), email, false, admin);
				})
				.toList();
	}

	public List<AdminProductRowDto> getBlacklistedProducts() {
		List<ItemEntity> items = itemRepository.findAllByStatus(STATUS_DELETED);
		List<java.util.UUID> sellerIds = items.stream()
				.map(ItemEntity::getSellerId)
				.distinct()
				.toList();
		Map<java.util.UUID, String> sellerNames = userRepository.findAllById(sellerIds).stream()
				.collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName, (a, b) -> a));

		return items.stream()
				.map(item -> {
					String sellerName = sellerNames.getOrDefault(item.getSellerId(), "—");
					return new AdminProductRowDto(
							item.getId(),
							item.getName(),
							item.getPriceAmount() != null ? item.getPriceAmount() : 0L,
							sellerName,
							"削除済");
				})
				.toList();
	}

	/**
	 * AIモデレーションで違反と判定された商品の一覧（違反リスト用）。
	 */
	public List<AdminProductRowDto> getViolationProducts() {
		List<ItemModerationEntity> records =
				itemModerationRepository.findByRejectedTrueOrderByCreatedAtDesc();
		List<UUID> itemIdsOrdered = records.stream()
				.map(ItemModerationEntity::getItemId)
				.distinct()
				.toList();
		if (itemIdsOrdered.isEmpty()) {
			return List.of();
		}
		List<ItemEntity> items = itemRepository.findAllById(itemIdsOrdered);
		Map<UUID, Integer> orderIndex = new java.util.HashMap<>();
		for (int i = 0; i < itemIdsOrdered.size(); i++) {
			orderIndex.put(itemIdsOrdered.get(i), i);
		}
		items.sort((a, b) -> Integer.compare(
				orderIndex.getOrDefault(a.getId(), Integer.MAX_VALUE),
				orderIndex.getOrDefault(b.getId(), Integer.MAX_VALUE)));
		List<UUID> sellerIds = items.stream()
				.map(ItemEntity::getSellerId)
				.distinct()
				.toList();
		Map<UUID, String> sellerNames = userRepository.findAllById(sellerIds).stream()
				.collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName, (a, b) -> a));

		return items.stream()
				.map(item -> {
					String sellerName = sellerNames.getOrDefault(item.getSellerId(), "—");
					return new AdminProductRowDto(
							item.getId(),
							item.getName(),
							item.getPriceAmount() != null ? item.getPriceAmount() : 0L,
							sellerName,
							"違反検知");
				})
				.toList();
	}

	private static final String TARGET_TYPE_ITEM = "ITEM";

	/**
	 * ユーザーから通報された商品の一覧（通報が1件以上ある商品）。通報が新しい順。
	 */
	public List<AdminProductRowDto> getReportedProducts() {
		List<ReportEntity> reports = reportRepository.findByTargetTypeOrderByCreatedAtDesc(TARGET_TYPE_ITEM);
		List<UUID> itemIdsOrdered = reports.stream()
				.map(ReportEntity::getTargetId)
				.distinct()
				.toList();
		if (itemIdsOrdered.isEmpty()) {
			return List.of();
		}
		List<ItemEntity> items = itemRepository.findAllById(itemIdsOrdered);
		Map<UUID, Integer> orderIndex = new java.util.HashMap<>();
		for (int i = 0; i < itemIdsOrdered.size(); i++) {
			orderIndex.put(itemIdsOrdered.get(i), i);
		}
		items.sort((a, b) -> Integer.compare(
				orderIndex.getOrDefault(a.getId(), Integer.MAX_VALUE),
				orderIndex.getOrDefault(b.getId(), Integer.MAX_VALUE)));
		List<UUID> sellerIds = items.stream()
				.map(ItemEntity::getSellerId)
				.distinct()
				.toList();
		Map<UUID, String> sellerNames = userRepository.findAllById(sellerIds).stream()
				.collect(Collectors.toMap(UserEntity::getId, UserEntity::getDisplayName, (a, b) -> a));

		return items.stream()
				.map(item -> {
					String sellerName = sellerNames.getOrDefault(item.getSellerId(), "—");
					String statusLabel = toStatusLabel(item.getStatus());
					return new AdminProductRowDto(
							item.getId(),
							item.getName(),
							item.getPriceAmount() != null ? item.getPriceAmount() : 0L,
							sellerName,
							"通報あり（" + statusLabel + "）");
				})
				.toList();
	}

	private static String toStatusLabel(String status) {
		if (status == null) return "—";
		return switch (status) {
			case "ACTIVE", "PUBLISHED" -> "公開中";
			case "DRAFT" -> "下書き";
			case "SUSPENDED" -> "出品停止";
			case "IN_TRADE" -> "取引中";
			case "SOLD" -> "売却済";
			case "DELETED" -> "削除済";
			default -> status;
		};
	}
}
