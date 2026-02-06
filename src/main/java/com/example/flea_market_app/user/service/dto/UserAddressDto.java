package com.example.flea_market_app.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ユーザー設定画面などで表示する配送先情報。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDto {

	private String recipientName;
	private String postalCode;
	private String address;
	private String phone;

	public static UserAddressDto empty() {
		return new UserAddressDto("", "", "", "");
	}

	public static UserAddressDto from(String recipientName, String postalCode, String address, String phone) {
		return new UserAddressDto(
				recipientName != null ? recipientName : "",
				postalCode != null ? postalCode : "",
				address != null ? address : "",
				phone != null ? phone : "");
	}
}
