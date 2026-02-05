package com.example.flea_market_app.user.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.flea_market_app.common.constant.ImageConstants;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;
import com.example.flea_market_app.user.controller.dto.FavoriteItemViewDto;
import com.example.flea_market_app.user.controller.dto.UserSettingsViewDto;
import com.example.flea_market_app.user.service.UserProfileQueryService;
import com.example.flea_market_app.user.service.dto.UserMeResponse;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserPageController {

	private static final Logger log = LoggerFactory.getLogger(UserPageController.class);

	private final UserProfileQueryService userProfileQueryService;
	private final FavoriteService favoriteService;

	@GetMapping("/user/settings")
	public String userSettings(Model model) {
		UUID userId = SecurityUtil.getCurrentUserId();
		log.info("User settings page displayed: userId={}", userId);
		UserMeResponse me = userProfileQueryService.getMe(userId);

		UserSettingsViewDto user = UserSettingsViewDto.builder()
				.iconUrl(me.getIconUrl())
				.displayTitle(me.getDisplayName())
				.username(me.getEmail() != null ? me.getEmail() : me.getDisplayName())
				.caption(me.getCaption() != null ? me.getCaption() : "")
				.build();

		List<FavoriteItemResponse> favorites = favoriteService.listByUser(userId);
		List<FavoriteItemViewDto> favoriteViews = favorites.stream()
				.map(f -> new FavoriteItemViewDto(
						f.getItemId(),
						f.getThumbnailUrl() != null ? f.getThumbnailUrl() : ImageConstants.NO_IMAGE_PATH,
						f.getName(),
						String.valueOf(f.getPriceAmount())))
				.toList();

		model.addAttribute("user", user);
		model.addAttribute("favorites", favoriteViews);
		// テンプレートで参照する設定・カード・住所等（未実装のためデフォルト値）
		model.addAttribute("userSettings", Map.of("notificationEnabled", false));
		model.addAttribute("creditCard", Map.of(
				"maskedNumber", "",
				"expireMonth", "",
				"expireYear", ""));
		model.addAttribute("userProducts", Collections.emptyList());
		model.addAttribute("notifications", Collections.emptyList());
		model.addAttribute("address", Map.of(
				"recipientName", "",
				"postalCode", "",
				"address", "",
				"phone", ""));

		return "user/user_settings";
	}
}
