package com.example.flea_market_app.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.domain.AuthUser;
import com.example.flea_market_app.auth.domain.RefreshToken;
import com.example.flea_market_app.auth.repository.RefreshTokenRepository;
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

	@Transactional
	public LoginResponse login(LoginRequest request) {

		// ユーザー不在でも404ではなく、401(認証情報の不足)
		AuthUser authUser;

		try {
			authUser = authUserProvider.loadByIdentifier(request.getIdentifier());
		} catch (RuntimeException e) {
			throw UnauthorizedBusinessException.invalidCredentials();
		}

		if (!passwordHasher.matches(request.getPassword(), authUser.getPasswordHash())) {
			// exceptionに関して要検討
			throw UnauthorizedBusinessException.invalidCredentials();
		}

		String accessToken = tokenService.generateAccessToken(authUser.getUserId());
		RefreshToken refreshToken = tokenService.generateRefreshToken(authUser.getUserId());

		refreshTokenRepository.deleteByUserId(authUser.getUserId());
		refreshTokenRepository.save(refreshToken);

		return new LoginResponse(
				authUser.getUserId(),
				accessToken,
				refreshToken.getToken());
	}

	@Transactional(readOnly = true)
	public RefreshResponse refresh(RefreshRequest request) {

		RefreshToken refreshToken = refreshTokenRepository
				.findByToken(request.getRefreshToken())
				.orElseThrow(UnauthorizedBusinessException::invalidRefreshToken);

		if (refreshToken.isExpired()) {
			throw UnauthorizedBusinessException.refreshTokenExpired();
		}

		String newAccessToken = tokenService.generateAccessToken(refreshToken.getUserId());

		return new RefreshResponse(newAccessToken);

	}
}
