package com.example.flea_market_app.user.dto;

/**
 * プロフィール画像アップロード成功時のレスポンスDTO。
 * 
 * <p>プロフィール画像のアップロードが成功した際に返却される情報を保持します。
 * S3キーと画像URLを含みます。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public class ProfileImageResponse {

	/** S3オブジェクトキー */
	private final String s3Key;

	/** 画像の公開URL */
	private final String imageUrl;

	/**
	 * コンストラクタ。
	 * 
	 * @param s3Key S3オブジェクトキー
	 * @param imageUrl 画像の公開URL
	 */
	public ProfileImageResponse(String s3Key, String imageUrl) {
		this.s3Key = s3Key;
		this.imageUrl = imageUrl;
	}

	/**
	 * S3オブジェクトキーを取得します。
	 * 
	 * @return S3オブジェクトキー
	 */
	public String getS3Key() {
		return s3Key;
	}

	/**
	 * 画像の公開URLを取得します。
	 * 
	 * @return 画像の公開URL
	 */
	public String getImageUrl() {
		return imageUrl;
	}

}
