package com.example.flea_market_app.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * プロフィール画像アップロード成功時のレスポンスDTO。
 * 
 * <p>プロフィール画像のアップロードが成功した際に返却される情報を保持します。
 * S3キーと画像URLを含みます。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class ProfileImageResponse {

	/** S3オブジェクトキー */
	private final String s3Key;

	/** 画像の公開URL */
	private final String imageUrl;

}
