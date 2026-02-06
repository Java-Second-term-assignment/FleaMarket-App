package com.example.flea_market_app.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * グラフ用：日付別件数。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DateCountDto {

	/** 日付文字列（yyyy-MM-dd） */
	private String date;
	private long count;
}
