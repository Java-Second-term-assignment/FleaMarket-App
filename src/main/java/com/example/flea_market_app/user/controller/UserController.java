package com.example.flea_market_app.user.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.config.security.SecurityUtil;
import com.example.flea_market_app.user.service.UserProfileQueryService;
import com.example.flea_market_app.user.service.UserService;
import com.example.flea_market_app.user.service.dto.UpdateProfileRequest;
import com.example.flea_market_app.user.service.dto.UserMeResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Validated
public class UserController {

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
		userService.updateProfile(userId, req.getDisplayName());
		return ResponseEntity.noContent().build();
	}
}
