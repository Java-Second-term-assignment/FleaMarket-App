package com.example.flea_market_app.admin.service.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeAdminRoleRequest {

	/**
	 * true: 付与 / false: 剥奪
	 * ※ 最小権限モデル。将来 roles 化するなら拡張する
	 */
	private boolean makeAdmin;

	@NotBlank
	private String reason;

	public boolean isMakeAdmin() {
		return makeAdmin;
	}
}
