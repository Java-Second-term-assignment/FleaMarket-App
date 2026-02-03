package com.example.flea_market_app.admin.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flea_market_app.admin.service.AdminService;
import com.example.flea_market_app.admin.service.AdminUserService;
import com.example.flea_market_app.admin.service.dto.ChangeAdminRoleRequest;
import com.example.flea_market_app.admin.service.dto.ForceDeleteItemRequest;
import com.example.flea_market_app.admin.service.dto.FreezeUserRequest;
import com.example.flea_market_app.config.security.SecurityUtil;

import lombok.RequiredArgsConstructor;

/**
 * 管理画面を代表してHTTPのやり取りをする入口。
 *
 * 責務:
 * - adminの認証情報（currentUserId）を Service に渡す
 * - DTO -> Service呼び出し -> HTTPレスポンスへ変換
 *
 * 禁止:
 * - ビジネス判断（状態遷移、if(違反っぽい)）
 * - repository 呼び出し
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

	private final AdminService adminService;
	private final AdminUserService adminUserService;

	/** 強制削除（論理削除）: POST /admin/items/{id}/force-delete */
	@PostMapping("/items/{id}/force-delete")
	public ResponseEntity<Void> forceDeleteItem(
			@PathVariable("id") UUID itemId,
			@Valid @RequestBody ForceDeleteItemRequest req) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminService.forceDeleteItem(currentUserId, itemId, req.getReason());
		return ResponseEntity.noContent().build();
	}

	/** ユーザー凍結: POST /admin/users/{id}/freeze */
	@PostMapping("/users/{id}/freeze")
	public ResponseEntity<Void> freezeUser(
			@PathVariable("id") UUID userId,
			@Valid @RequestBody FreezeUserRequest req) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.freezeUser(currentUserId, userId, req.getReason(), null);
		return ResponseEntity.noContent().build();
	}

	/** 強制退会（MVPでは凍結扱い）: POST /admin/users/{id}/force-withdraw */
	@PostMapping("/users/{id}/force-withdraw")
	public ResponseEntity<Void> forceWithdraw(
			@PathVariable("id") UUID userId,
			@Valid @RequestBody FreezeUserRequest req) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.forceWithdraw(currentUserId, userId, req.getReason());
		return ResponseEntity.noContent().build();
	}

	/** admin権限付与/剥奪: POST /admin/users/{id}/admin-role */
	@PostMapping("/users/{id}/admin-role")
	public ResponseEntity<Void> changeAdminRole(
			@PathVariable("id") UUID userId,
			@Valid @RequestBody ChangeAdminRoleRequest req) {
		UUID currentUserId = SecurityUtil.getCurrentUserId();
		adminUserService.changeAdminRole(currentUserId, userId, req.isMakeAdmin(), req.getReason());
		return ResponseEntity.noContent().build();
	}
}
