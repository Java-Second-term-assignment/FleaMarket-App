package com.example.flea_market_app.admin.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 管理者ダッシュボード「ユーザー一覧」の1行分。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRowDto {

	private UUID id;
	private String username;
	private String email;
	private boolean enabled;
	private boolean admin;
}
