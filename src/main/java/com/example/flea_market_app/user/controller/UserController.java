package com.example.flea_market_app.user.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.user.service.UserProfileQueryService;
import com.example.flea_market_app.user.service.UserService;
import com.example.flea_market_app.user.service.dto.ProfileImageResponse;
import com.example.flea_market_app.user.service.dto.UpdateProfileRequest;
import com.example.flea_market_app.user.service.dto.UserMeResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Validated
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	private final UserProfileQueryService userProfileQueryService;
	private final UserService userService;

	// userIdはSecurityContextからのみ取得（引数で受け取らない）
	@GetMapping("/me")
	public ResponseEntity<UserMeResponse> getMe() {
		UUID userId = SecurityUtil.getCurrentUserId();
		return ResponseEntity.ok(userProfileQueryService.getMe(userId));
	}

	@PatchMapping("/me")
	public ResponseEntity<Void> updateMe(@Validated @RequestBody UpdateProfileRequest req) {
		UUID userId = SecurityUtil.getCurrentUserId();
		userService.updateProfile(userId, req.getDisplayName(), req.getCaption());
		return ResponseEntity.noContent().build();
	}

	/**
	 * プロフィール画像をアップロード・更新します。
	 * 
	 * <p>認証済みユーザーが自分のプロフィール画像を設定・更新できます。
	 * 既存の画像がある場合は、自動的に削除されてから新しい画像がアップロードされます。
	 * 
	 * @param image アップロードする画像ファイル
	 * @return プロフィール画像の情報（S3キー、URL等）
	 */
	@PutMapping("/me/profile-image")
	public ResponseEntity<ProfileImageResponse> updateProfileImage(
			@RequestParam("image") MultipartFile image) {

		log.info("Received profile image upload request");

		UUID currentUserId = SecurityUtil.getCurrentUserId();
		ProfileImageResponse response = userService.updateProfileImage(currentUserId, currentUserId, image);

		log.info("Successfully updated profile image for user: {}", currentUserId);

		return ResponseEntity.ok(response);
	}
}
