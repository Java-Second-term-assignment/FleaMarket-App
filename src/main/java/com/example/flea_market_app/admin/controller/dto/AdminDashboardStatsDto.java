package com.example.flea_market_app.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 管理画面ダッシュボード用の統計情報。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDto {

	/** マーケットに公開中の商品数（PUBLISHED） */
	private long totalActiveItems;

	/** 登録ユーザー総数 */
	private long totalUsers;
}
