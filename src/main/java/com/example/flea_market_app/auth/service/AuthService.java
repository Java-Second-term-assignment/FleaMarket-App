package com.example.flea_market_app.auth.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.domain.AuthUser;
import com.example.flea_market_app.auth.domain.RefreshTokenEntity;
import com.example.flea_market_app.auth.repository.RefreshTokenRepository;
import com.example.flea_market_app.auth.service.TokenService.IssuedRefreshToken;
import com.example.flea_market_app.auth.service.dto.LoginRequest;
import com.example.flea_market_app.auth.service.dto.LoginResponse;
import com.example.flea_market_app.auth.service.dto.RefreshRequest;
import com.example.flea_market_app.auth.service.dto.RefreshResponse;
import com.example.flea_market_app.common.exception.UnauthorizedBusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final TokenService tokenService;
	private final RefreshTokenRepository refreshTokenRepository;

	private final AuthUserProvider authUserProvider;
	private final PasswordHasher passwordHasher;
	private final TokenHasher tokenHasher;

	@Transactional
	public LoginResponse login(LoginRequest request) {

		AuthUser authUser;
		try {
			authUser = authUserProvider.loadByIdentifier(request.getIdentifier());
		} catch (RuntimeException e) {
			throw UnauthorizedBusinessException.invalidCredentials();
		}

		if (!passwordHasher.matches(request.getPassword(), authUser.getPasswordHash())) {
			throw UnauthorizedBusinessException.invalidCredentials();
		}

		// userId を UUID に変換（DBがuuidのため）
		UUID userId = UUID.fromString(authUser.getUserId());

		String accessToken = tokenService.generateAccessToken(userId);

		IssuedRefreshToken issued = tokenService.issueRefreshToken(userId);

		refreshTokenRepository.deleteByUserId(userId);
		refreshTokenRepository.save(issued.getEntity());

		return new LoginResponse(
				authUser.getUserId(),
				accessToken,
				issued.getRawToken());
	}

	@Transactional(readOnly = true)
	public RefreshResponse refresh(RefreshRequest request) {

		String tokenHash = tokenHasher.hash(request.getRefreshToken());

		RefreshTokenEntity entity = refreshTokenRepository
				.findByTokenHash(tokenHash)
				.orElseThrow(UnauthorizedBusinessException::invalidRefreshToken);

		OffsetDateTime now = OffsetDateTime.now();
		if (entity.isRevoked() || entity.isExpired(now)) {
			throw UnauthorizedBusinessException.refreshTokenExpired();
		}

		String newAccessToken = tokenService.generateAccessToken(entity.getUserId());
		return new RefreshResponse(newAccessToken);
	}
}
