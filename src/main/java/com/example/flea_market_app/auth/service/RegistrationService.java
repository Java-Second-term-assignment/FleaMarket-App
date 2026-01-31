package com.example.flea_market_app.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.auth.controller.dto.RegisterForm;
import com.example.flea_market_app.auth.domain.AuthUserEntity;
import com.example.flea_market_app.auth.repository.AuthUserRepository;
import com.example.flea_market_app.common.exception.ValidationBusinessException;
import com.example.flea_market_app.common.validation.EmailValidator;
import com.example.flea_market_app.common.validation.PasswordPolicy;
import com.example.flea_market_app.user.domain.UserEntity;
import com.example.flea_market_app.user.domain.VerificationStatus;
import com.example.flea_market_app.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {

	private static final short INITIAL_RANK_ID = 1;

	private final AuthUserRepository authUserRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public void register(RegisterForm form) {
		if (!form.getPassword().equals(form.getConfirmPassword())) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_PASSWORD,
					"パスワードとパスワード確認が一致しません");
		}

		EmailValidator.validate(form.getEmail());
		PasswordPolicy.validate(form.getPassword());

		if (authUserRepository.findByEmail(form.getEmail()).isPresent()) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INVALID_EMAIL,
					"このメールアドレスは既に登録されています");
		}

		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID());
		user.setDisplayName(extractDisplayName(form.getEmail()));
		user.setUserRankId(INITIAL_RANK_ID);
		user.setIdentityStatus(VerificationStatus.UNVERIFIED.name());
		user.setActive(true);
		user.setProfileImageS3Key(null);
		userRepository.save(user);

		AuthUserEntity authUser = new AuthUserEntity();
		authUser.setId(UUID.randomUUID());
		authUser.setUserId(user.getId());
		authUser.setEmail(form.getEmail());
		authUser.setPasswordHash(passwordEncoder.encode(form.getPassword()));
		authUser.setAdmin(false);
		authUserRepository.save(authUser);
	}

	private String extractDisplayName(String email) {
		int at = email.indexOf('@');
		return at > 0 ? email.substring(0, at) : email;
	}
}
