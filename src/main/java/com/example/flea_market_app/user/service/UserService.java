package com.example.flea_market_app.user.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.user.dto.ProfileImageResponse;

/**
 * ユーザーサービスのインターフェース。
 * 
 * <p>ユーザー関連のビジネスロジックを提供します。
 * テスト容易性のためインターフェースで分離されています。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public interface UserService {

	/**
	 * プロフィール画像を更新します。
	 * 
	 * <p>認証済みユーザーが自分のプロフィール画像のみ更新可能です。
	 * 既存の画像がある場合は、S3から削除してから新しい画像をアップロードします。
	 * 
	 * @param userId ユーザーID（認証済みユーザーのIDである必要がある）
	 * @param imageFile アップロードする画像ファイル
	 * @return プロフィール画像の情報（S3キー、URL等）
	 * @throws com.example.flea_market_app.common.exception.BusinessException バリデーションエラー、認証エラー、S3操作エラー等
	 */
	ProfileImageResponse updateProfileImage(UUID userId, MultipartFile imageFile);

}
