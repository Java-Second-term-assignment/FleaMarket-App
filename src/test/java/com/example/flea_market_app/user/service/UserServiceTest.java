package com.example.flea_market_app.user.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.service.S3ImageService;
import com.example.flea_market_app.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserRankService userRankService;

	@Mock
	private S3ImageService s3ImageService;

	@InjectMocks
	private UserService userService;

	@Test
	void getRequired_whenUserNotFound_throwsNotFoundBusinessException() {
		UUID userId = UUID.randomUUID();
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getRequired(userId))
				.isInstanceOf(NotFoundBusinessException.class);
	}
}
