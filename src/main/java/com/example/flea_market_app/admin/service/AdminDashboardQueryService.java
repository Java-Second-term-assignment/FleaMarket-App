package com.example.flea_market_app.admin.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.flea_market_app.admin.controller.dto.AdminProductRowDto;
import com.example.flea_market_app.admin.controller.dto.AdminUserRowDto;
import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * 管理者ダッシュボード表示用のユーザー一覧・商品一覧を取得する。
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardQueryService {

	private final UserRepository userRepository;
	private final AuthUserRepository authUserRepository;
	private final ItemRepository itemRepository;

	public List<AdminUserRowDto> getUsersForDashboard() {
		List<UserEntity> users = userRepository.findAll();
		List<AuthUserEntity> authUsers = authUserRepository.findAll();
		Map<java.util.UUID, AuthUserEntity> authByUserId = authUsers.stream()
				.collect(Collectors.toMap(AuthUserEntity::getUserId, a -> a, (a, b) -> a));

		return users.stream()
				.map(u -> {
					AuthUserEntity auth = authByUserId.get(u.getId());
					String email = auth != null ? auth.getEmail() : "—";
					return new AdminUserRowDto(u.getId(), u.getDisplayName(), email, u.isActive());
				})
				.toList();
	}

	public List<AdminProductRowDto> getProductsForDashboard() {
		List<ItemEntity> items = itemRepository.findAll();
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

	private static String toStatusLabel(String status) {
		if (status == null) return "—";
		return switch (status) {
			case "ACTIVE", "PUBLISHED" -> "公開中";
			case "DRAFT" -> "下書き";
			case "DELETED" -> "削除済";
			default -> status;
		};
	}
}
