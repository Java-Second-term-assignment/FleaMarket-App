package com.example.flea_market_app.catalog.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

/**
 * 商品画像のアップロード・取得・削除を行うサービスのインターフェース。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public interface ItemImageService {

	/**
	 * 複数画像をアップロードしてS3キーのリストを返します。
	 * 
	 * <p>画像はアップロード順にdisplay_orderが設定されます（0から開始）。
	 * 1枚目の画像（display_order=0）がサムネイル画像として使用されます。
	 * 
	 * @param itemId 商品ID
	 * @param files アップロードする画像ファイルのリスト（1-10枚）
	 * @return アップロードされたS3キーのリスト
	 * @throws com.example.flea_market_app.common.exception.BusinessException バリデーションエラー、S3操作エラー等
	 */
	List<String> uploadItemImages(UUID itemId, List<MultipartFile> files);

	/**
	 * 商品の画像URLリストを取得します（表示順でソート）。
	 * 
	 * @param itemId 商品ID
	 * @return 画像URLのリスト（display_orderの昇順）
	 */
	List<String> getItemImageUrls(UUID itemId);

	/**
	 * サムネイル画像（1枚目）のURLを取得します。
	 * 
	 * @param itemId 商品ID
	 * @return サムネイル画像のURL。画像が存在しない場合はnull
	 */
	String getThumbnailImageUrl(UUID itemId);

	/**
	 * 商品の1枚目の画像のS3オブジェクトキーを取得します（モデレーション等でRekognitionに渡す用）。
	 * 
	 * @param itemId 商品ID
	 * @return 1枚目のS3キー。画像が存在しない場合はnull
	 */
	String getFirstImageS3Key(UUID itemId);

	/**
	 * 商品の全画像をS3とDBから削除します。
	 * 
	 * @param itemId 商品ID
	 */
	void deleteItemImages(UUID itemId);
}
