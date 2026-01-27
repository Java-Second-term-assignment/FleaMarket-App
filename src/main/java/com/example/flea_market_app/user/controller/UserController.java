package com.example.flea_market_app.user.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.common.response.ApiResponse;
import com.example.flea_market_app.common.security.SecurityUtils;
import com.example.flea_market_app.user.dto.ProfileImageResponse;
import com.example.flea_market_app.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * ユーザー関連のREST APIコントローラー。
 * 
 * <p>ユーザー情報の取得・更新などのエンドポイントを提供します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private static final Logger log = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;

	/**
	 * プロフィール画像をアップロード・更新します。
	 * 
	 * <p>認証済みユーザーが自分のプロフィール画像を設定・更新できます。
	 * 既存の画像がある場合は、自動的に削除されてから新しい画像がアップロードされます。
	 * 
	 * <p>リクエスト例:
	 * <pre>
	 * PUT /api/users/me/profile-image
	 * Content-Type: multipart/form-data
	 * 
	 * image: [バイナリデータ]
	 * </pre>
	 * 
	 * @param image アップロードする画像ファイル
	 * @return プロフィール画像の情報（S3キー、URL等）
	 */
	@PutMapping("/me/profile-image")
	public ResponseEntity<ApiResponse<ProfileImageResponse>> updateProfileImage(
			@RequestParam("image") MultipartFile image) {

		log.info("Received profile image upload request");

		UUID currentUserId = SecurityUtils.getCurrentUserId();
		ProfileImageResponse response = userService.updateProfileImage(currentUserId, image);

		log.info("Successfully updated profile image for user: {}", currentUserId);

		return ResponseEntity
				.status(HttpStatus.OK)
				.body(ApiResponse.success(response));
	}

}
