package com.example.flea_market_app.user.service.impl;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.security.SecurityUtils;
import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.common.validation.ImageValidator;
import com.example.flea_market_app.user.domain.User;
import com.example.flea_market_app.user.dto.ProfileImageResponse;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * ユーザーサービスの実装クラス。
 * 
 * <p>UserServiceインターフェースの実装です。
 * ユーザー関連のビジネスロジックを提供します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final UserRepository userRepository;
	private final S3ImageService s3ImageService;

	@Value("${aws.s3.bucket.name}")
	private String bucketName;

	@Value("${aws.s3.profile-image.max-size:5242880}")
	private Long maxImageSize;

	@Override
	@Transactional
	public ProfileImageResponse updateProfileImage(UUID userId, MultipartFile imageFile) {
		log.info("Updating profile image for user: {}", userId);

		// 認証チェック: 認証済みユーザーが自分の画像のみ更新可能
		UUID currentUserId = SecurityUtils.getCurrentUserId();
		if (!currentUserId.equals(userId)) {
			log.warn("User {} attempted to update profile image for user {}", currentUserId, userId);
			throw new AccessDeniedBusinessException();
		}

		// バリデーション
		ImageValidator.validate(imageFile, maxImageSize);

		// ユーザー存在確認
		User user = userRepository.findById(userId)
				.orElseThrow(() -> {
					log.warn("User not found: {}", userId);
					return NotFoundBusinessException.of(ResourceType.USER);
				});

		// ファイル拡張子の取得
		String originalFilename = imageFile.getOriginalFilename();
		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new IllegalArgumentException("Original filename is null or empty");
		}
		int lastDotIndex = originalFilename.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == originalFilename.length() - 1) {
			throw new IllegalArgumentException("File extension not found");
		}
		String fileExtension = originalFilename.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);

		// 古い画像の削除（存在する場合）
		String oldS3Key = user.getProfileImageS3Key();
		if (oldS3Key != null && !oldS3Key.isEmpty()) {
			try {
				s3ImageService.deleteImage(bucketName, oldS3Key);
				log.info("Deleted old profile image from S3: {}", oldS3Key);
			} catch (Exception e) {
				// 削除失敗は警告ログを記録するが、処理は継続
				log.warn("Failed to delete old profile image from S3: {}. Continuing with new upload.", oldS3Key, e);
			}
		}

		// 新しいS3キーの生成
		String newS3Key = s3ImageService.generateProfileImageKey(userId, fileExtension);

		// S3にアップロード
		try {
			s3ImageService.uploadImage(
					bucketName,
					newS3Key,
					imageFile.getInputStream(),
					imageFile.getContentType(),
					imageFile.getSize());
			log.info("Successfully uploaded profile image to S3: {}", newS3Key);
		} catch (IOException e) {
			log.error("Failed to read image file for upload: userId={}", userId, e);
			throw new RuntimeException("Failed to read image file", e);
		}

		// データベース更新
		user.setProfileImageS3Key(newS3Key);
		userRepository.save(user);
		log.info("Successfully updated profile image in database for user: {}", userId);

		// レスポンス生成
		String imageUrl = s3ImageService.generateImageUrl(bucketName, newS3Key);
		return new ProfileImageResponse(newS3Key, imageUrl);
	}

}
