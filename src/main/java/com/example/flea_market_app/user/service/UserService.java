package com.example.flea_market_app.user.service;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
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
import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.common.validation.ImageValidator;
import com.example.flea_market_app.user.domain.User;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.VerificationStatus;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.service.dto.ProfileImageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final UserRankService userRankService;
	private final S3ImageService s3ImageService;

	@Value("${aws.s3.bucket.name}")
	private String bucketName;

	@Value("${aws.s3.profile-image.max-size:5242880}")
	private Long maxImageSize;

	@Transactional(readOnly = true)
	public User getRequired(UUID userId) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		return toDomain(e);
	}

	@Transactional
	public void updateProfile(UUID userId, String displayName, String caption) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		e.setDisplayName(displayName != null ? displayName.trim() : "");
		e.setCaption(caption != null ? caption.trim() : null);
		userRepository.save(e);
	}

	/**
	 * ユーザーの既定配送先を更新します。呼び出し元で認証ユーザー本人であることを確認すること。
	 */
	@Transactional
	public void updateAddress(UUID userId, String recipientName, String postalCode, String address, String phone) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		e.setRecipientName(trimToNull(recipientName, 100));
		e.setPostalCode(trimToNull(postalCode, 20));
		e.setAddress(trimToNull(address, 500));
		e.setPhone(trimToNull(phone, 30));
		userRepository.save(e);
	}

	/**
	 * 注文フロー用の既定配送先を返します。キーは name, postcode, fullAddress。
	 * 未設定の場合は空文字の Map を返します。
	 */
	@Transactional(readOnly = true)
	public Map<String, String> getDefaultShippingAddress(UUID userId) {
		UserEntity e = userRepository.findById(userId).orElse(null);
		if (e == null) {
			return Map.of("name", "", "postcode", "", "fullAddress", "");
		}
		return Map.of(
				"name", nullToEmpty(e.getRecipientName()),
				"postcode", nullToEmpty(e.getPostalCode()),
				"fullAddress", nullToEmpty(e.getAddress()));
	}

	private static String trimToNull(String value, int maxLen) {
		if (value == null) {
			return null;
		}
		String t = value.trim();
		if (t.isEmpty()) {
			return null;
		}
		return t.length() > maxLen ? t.substring(0, maxLen) : t;
	}

	private static String nullToEmpty(String value) {
		return value != null ? value : "";
	}

	@Transactional
	public void submitVerification(UUID userId) {
		UserEntity e = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		// 状態遷移の妥当性は domain でチェックしたいので一度 domain 化してもOK
		User user = toDomain(e);
		user.submitVerification();

		e.setIdentityStatus(user.getVerificationStatus().name());
		userRepository.save(e);
	}

	/**
	 * プロフィール画像を更新します。
	 * 
	 * <p>認証済みユーザーが自分のプロフィール画像のみ更新可能です。
	 * 既存の画像がある場合は、S3から削除してから新しい画像をアップロードします。
	 * 
	 * @param userId ユーザーID（認証済みユーザーのIDである必要がある）
	 * @param currentUserId 現在認証されているユーザーID（SecurityUtilから取得）
	 * @param imageFile アップロードする画像ファイル
	 * @return プロフィール画像の情報（S3キー、URL等）
	 * @throws com.example.flea_market_app.common.exception.BusinessException バリデーションエラー、認証エラー、S3操作エラー等
	 */
	@Transactional
	public ProfileImageResponse updateProfileImage(UUID userId, UUID currentUserId, MultipartFile imageFile) {
		log.info("Updating profile image for user: {}", userId);

		// 認証チェック: 認証済みユーザーが自分の画像のみ更新可能
		if (!currentUserId.equals(userId)) {
			log.warn("User {} attempted to update profile image for user {}", currentUserId, userId);
			throw new AccessDeniedBusinessException();
		}

		// バリデーション
		ImageValidator.validate(imageFile, maxImageSize);

		// ユーザー存在確認
		UserEntity e = userRepository.findById(userId)
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
		String oldS3Key = e.getProfileImageS3Key();
		if (oldS3Key != null && !oldS3Key.isEmpty()) {
			try {
				s3ImageService.deleteImage(bucketName, oldS3Key);
				log.info("Deleted old profile image from S3: {}", oldS3Key);
			} catch (Exception ex) {
				// 削除失敗は警告ログを記録するが、処理は継続
				log.warn("Failed to delete old profile image from S3: {}. Continuing with new upload.", oldS3Key, ex);
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
		} catch (IOException ex) {
			log.error("Failed to read image file for upload: userId={}", userId, ex);
			throw new RuntimeException("Failed to read image file", ex);
		}

		// データベース更新
		e.setProfileImageS3Key(newS3Key);
		userRepository.save(e);
		log.info("Successfully updated profile image in database for user: {}", userId);

		// レスポンス生成
		String imageUrl = s3ImageService.generateImageUrl(bucketName, newS3Key);
		return new ProfileImageResponse(newS3Key, imageUrl);
	}

	private User toDomain(UserEntity e) {
		return new User(
				e.getId(),
				e.getDisplayName(),
				VerificationStatus.valueOf(e.getIdentityStatus()),
				userRankService.loadRank(e.getUserRankId()),
				e.isActive());
	}
}
