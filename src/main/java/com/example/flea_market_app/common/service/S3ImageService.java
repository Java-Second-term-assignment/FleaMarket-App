package com.example.flea_market_app.common.service;

import java.io.InputStream;
import java.util.UUID;

/**
 * S3への画像アップロード・削除・URL生成を行うサービスのインターフェース。
 * 
 * <p>テスト容易性のためインターフェースで分離されています。
 * 実装クラスはS3ImageServiceImplを参照してください。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public interface S3ImageService {

	/**
	 * 画像をS3にアップロードします。
	 * 
	 * @param bucketName S3バケット名
	 * @param s3Key S3オブジェクトキー（例: profiles/{userId}/{uuid}.{ext}）
	 * @param inputStream 画像データの入力ストリーム
	 * @param contentType コンテンツタイプ（例: image/jpeg）
	 * @param contentLength コンテンツ長（バイト）
	 * @return アップロードされたS3オブジェクトキー
	 * @throws com.example.flea_market_app.common.exception.BusinessException アップロードに失敗した場合
	 */
	String uploadImage(String bucketName, String s3Key, InputStream inputStream, String contentType, long contentLength);

	/**
	 * S3から画像を削除します。
	 * 
	 * @param bucketName S3バケット名
	 * @param s3Key 削除するS3オブジェクトキー
	 * @throws com.example.flea_market_app.common.exception.BusinessException 削除に失敗した場合
	 */
	void deleteImage(String bucketName, String s3Key);

	/**
	 * S3オブジェクトの公開URLを生成します。
	 * 
	 * <p>注意: このメソッドは公開読み取り可能なオブジェクトに対してのみ有効です。
	 * プライベートオブジェクトの場合は、署名付きURLを生成する必要があります。
	 * 
	 * @param bucketName S3バケット名
	 * @param s3Key S3オブジェクトキー
	 * @return 公開URL
	 */
	String generateImageUrl(String bucketName, String s3Key);

	/**
	 * プロフィール画像用のS3キーを生成します。
	 * 
	 * <p>命名規則: profiles/{userId}/{uuid}.{ext}
	 * 
	 * @param userId ユーザーID
	 * @param fileExtension ファイル拡張子（ドットを含まない、例: jpg）
	 * @return S3オブジェクトキー
	 */
	default String generateProfileImageKey(UUID userId, String fileExtension) {
		UUID fileId = UUID.randomUUID();
		return String.format("profiles/%s/%s.%s", userId, fileId, fileExtension);
	}

	/**
	 * 商品画像用のS3キーを生成します。
	 * 
	 * <p>命名規則: items/{itemId}/{uuid}.{ext}
	 * 
	 * @param itemId 商品ID
	 * @param fileExtension ファイル拡張子（ドットを含まない、例: jpg）
	 * @return S3オブジェクトキー
	 */
	default String generateItemImageKey(UUID itemId, String fileExtension) {
		UUID fileId = UUID.randomUUID();
		return String.format("items/%s/%s.%s", itemId, fileId, fileExtension);
	}

}
