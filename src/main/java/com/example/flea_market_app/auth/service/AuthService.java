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

		// 認証対象の取得(仮) (あとでUserService等に差し替え)
		AuthUser authUser = authUserProvider.loadByIdentifier(request.getIdentifier());

		if (!passwordHasher.matches(request.getPassword(), authUser.getPasswordHash())) {
			// exceptionに関して要検討
			throw new IllegalArgumentException("Invalid credentials");
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
				.orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

		if (refreshToken.isExpired()) {
			throw new IllegalStateException("Refresh token expired");
		}

		String newAccessToken = tokenService.generateAccessToken(refreshToken.getUserId());

		return new RefreshResponse(newAccessToken);

	}
}
