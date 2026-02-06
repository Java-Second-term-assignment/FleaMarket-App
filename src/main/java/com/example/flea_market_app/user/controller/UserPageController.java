package com.example.flea_market_app.user.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.flea_market_app.common.constant.ImageConstants;
import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.engagement.favorite.service.FavoriteService;
import com.example.flea_market_app.engagement.favorite.service.dto.FavoriteItemResponse;
import com.example.flea_market_app.engagement.notification.service.NotificationService;
import com.example.flea_market_app.user.controller.dto.FavoriteItemViewDto;
import com.example.flea_market_app.user.controller.dto.UserProductItemDto;
import com.example.flea_market_app.user.controller.dto.UserSettingsViewDto;
import com.example.flea_market_app.user.service.UserProfileQueryService;
import com.example.flea_market_app.user.service.UserProductsQueryService;
import com.example.flea_market_app.user.service.UserService;
import com.example.flea_market_app.user.service.dto.UpdateAddressRequest;
import com.example.flea_market_app.user.service.dto.UserMeResponse;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserPageController {

	private static final Logger log = LoggerFactory.getLogger(UserPageController.class);

	private final UserProfileQueryService userProfileQueryService;
	private final UserService userService;
	private final FavoriteService favoriteService;
	private final UserProductsQueryService userProductsQueryService;
	private final NotificationService notificationService;

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

		List<UserProductItemDto> userProducts = userProductsQueryService.listBySeller(userId);
		var notifications = notificationService.listByUser(userId, 50);

		model.addAttribute("user", user);
		model.addAttribute("favorites", favoriteViews);
		model.addAttribute("userSettings", Map.of("notificationEnabled", me.isNotificationEnabled()));
		model.addAttribute("creditCard", Map.of(
				"maskedNumber", "",
				"expireMonth", "",
				"expireYear", ""));
		model.addAttribute("userProducts", userProducts);
		model.addAttribute("notifications", notifications);
		model.addAttribute("address", me.getAddress());

		return "user/user_settings";
	}

	@PostMapping("/user/address/update")
	public String updateAddress(
			@Valid @ModelAttribute UpdateAddressRequest req,
			BindingResult result,
			RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();
		if (result.hasErrors()) {
			ra.addFlashAttribute("errorMessage", "入力内容を確認してください。");
			return "redirect:/user/settings";
		}
		userService.updateAddress(
				userId,
				req.getRecipientName(),
				req.getPostalCode(),
				req.getAddress(),
				req.getPhone());
		ra.addFlashAttribute("message", "配送先を更新しました。");
		return "redirect:/user/settings";
	}

	@PostMapping("/user/settings/notification")
	public String updateNotificationSetting(
			@RequestParam(name = "notificationEnabled", required = false) String notificationEnabled,
			RedirectAttributes ra) {
		UUID userId = SecurityUtil.getCurrentUserId();
		boolean enabled = "true".equalsIgnoreCase(notificationEnabled);
		userService.updateNotificationEnabled(userId, enabled);
		ra.addFlashAttribute("message", "設定を保存しました。");
		return "redirect:/user/settings";
	}
}
