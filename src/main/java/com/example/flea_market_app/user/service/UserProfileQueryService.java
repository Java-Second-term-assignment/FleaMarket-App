package com.example.flea_market_app.user.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.user.repository.UserRepository;
import com.example.flea_market_app.user.repository.UserRepository.ProfileForMeProjection;
import com.example.flea_market_app.user.service.dto.UserAddressDto;
import com.example.flea_market_app.user.service.dto.UserMeResponse;
import com.example.flea_market_app.user.service.port.AuthAccountQueryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileQueryService {

	private final UserService userService;
	private final AuthAccountQueryPort authAccountQueryPort;
	private final UserRepository userRepository;
	private final S3ImageService s3ImageService;

	@Value("${aws.s3.bucket.name}")
	private String bucketName;

	@Transactional(readOnly = true)
	public UserMeResponse getMe(UUID userId) {
		var user = userService.getRequired(userId);
		String email = authAccountQueryPort.findEmailByUserId(userId).orElse(null);

		ProfileForMeProjection profile = userRepository.findProfileForMeByUserId(userId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.USER));
		String profileImageUrl = profile.getProfileImageUrl();
		if (profileImageUrl == null || profileImageUrl.isEmpty()) {
			String profileImageS3Key = profile.getProfileImageS3Key();
			profileImageUrl = (profileImageS3Key != null && !profileImageS3Key.isEmpty())
					? s3ImageService.generateImageUrl(bucketName, profileImageS3Key)
					: null;
		}
		String iconUrl = profileImageUrl;
		String caption = profile.getCaption() != null ? profile.getCaption() : "";

		UserAddressDto address = UserAddressDto.from(
				profile.getRecipientName(),
				profile.getPostalCode(),
				profile.getAddress(),
				profile.getPhone());

		// notification_enabled を参照しないため、カラム未追加のDBではデフォルト true
		return UserMeResponse.of(user, email, iconUrl, caption, address, true);
	}
}
