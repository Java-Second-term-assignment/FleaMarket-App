package com.example.flea_market_app.user.service.dto;

import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAddressRequest {

	@Size(max = 100)
	private String recipientName;

	@Size(max = 20)
	private String postalCode;

	@Size(max = 500)
	private String address;

	@Size(max = 30)
	private String phone;
}
