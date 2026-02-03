package com.example.flea_market_app.admin.controller.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 管理者ダッシュボード「商品一覧」の1行分。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRowDto {

	private UUID id;
	private String name;
	private long price;
	private String sellerName;
	/** 表示用ラベル（公開中・下書き・削除済 など） */
	private String statusLabel;
}
