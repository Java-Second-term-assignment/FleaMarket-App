package com.example.flea_market_app.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * グラフ用：ステータス別件数。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusCountDto {

	private String status;
	private String label;
	private long count;
}
