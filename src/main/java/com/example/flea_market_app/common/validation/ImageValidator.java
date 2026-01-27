package com.example.flea_market_app.common.validation;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.common.error.ErrorCode;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

/**
 * 画像ファイルのバリデーションを行うクラス。
 * 
 * <p>既存のEmailValidator、PasswordPolicyと同様のパターンで実装されています。
 * ファイル形式、サイズ、MIMEタイプ、ファイル名のサニタイズを検証します。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public class ImageValidator {

	/** 許可されるMIMEタイプ */
	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
			"image/jpeg",
			"image/png",
			"image/gif",
			"image/webp"
	);

	/** 許可されるファイル拡張子（小文字） */
	private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
			"jpg", "jpeg", "png", "gif", "webp"
	);

	/** デフォルトの最大ファイルサイズ（5MB） */
	private static final long DEFAULT_MAX_SIZE = 5 * 1024 * 1024; // 5MB

	/**
	 * 画像ファイルをバリデーションします。
	 * 
	 * <p>以下の項目を検証します：
	 * <ul>
	 *   <li>ファイルがnullでないこと</li>
	 *   <li>ファイルが空でないこと</li>
	 *   <li>ファイルサイズが上限以下であること（デフォルト5MB）</li>
	 *   <li>MIMEタイプが許可されていること</li>
	 *   <li>ファイル拡張子が許可されていること</li>
	 *   <li>MIMEタイプとファイル拡張子の整合性</li>
	 *   <li>ファイル名に危険な文字（../等）が含まれていないこと</li>
	 * </ul>
	 * 
	 * @param file 検証する画像ファイル
	 * @param maxSize 最大ファイルサイズ（バイト）。nullの場合はデフォルト値（5MB）を使用
	 * @throws ValidationBusinessException バリデーションに失敗した場合
	 */
	public static void validate(MultipartFile file, Long maxSize) {
		if (file == null || file.isEmpty()) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}

		long maxFileSize = maxSize != null ? maxSize : DEFAULT_MAX_SIZE;
		if (file.getSize() > maxFileSize) {
			throw new ValidationBusinessException(
					ErrorCode.IMAGE_TOO_LARGE,
					"error.image_too_large");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}

		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.isEmpty()) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}

		// ファイル名のサニタイズ（パス操作攻撃対策）
		String sanitizedFilename = sanitizeFilename(originalFilename);
		if (!sanitizedFilename.equals(originalFilename)) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}

		// ファイル拡張子の取得と検証
		String extension = getFileExtension(originalFilename);
		if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}

		// MIMEタイプとファイル拡張子の整合性チェック
		if (!isMimeTypeAndExtensionConsistent(contentType, extension)) {
			throw new ValidationBusinessException(
					ErrorCode.INVALID_IMAGE_FORMAT,
					"error.invalid_image_format");
		}
	}

	/**
	 * デフォルトの最大ファイルサイズ（5MB）で画像ファイルをバリデーションします。
	 * 
	 * @param file 検証する画像ファイル
	 * @throws ValidationBusinessException バリデーションに失敗した場合
	 */
	public static void validate(MultipartFile file) {
		validate(file, null);
	}

	/**
	 * ファイル名をサニタイズします。
	 * 
	 * <p>パス操作攻撃（../等）を防ぐため、危険な文字を含むファイル名を検出します。
	 * 
	 * @param filename 元のファイル名
	 * @return サニタイズされたファイル名（危険な文字が含まれている場合は正規化されたパス）
	 */
	private static String sanitizeFilename(String filename) {
		try {
			// Paths.get()を使用してパスを正規化し、危険な文字（../等）を検出
			return Paths.get(filename).normalize().toString();
		} catch (Exception e) {
			// パス解析に失敗した場合は元のファイル名を返す
			return filename;
		}
	}

	/**
	 * ファイル名から拡張子を取得します。
	 * 
	 * @param filename ファイル名
	 * @return 拡張子（ドットを含まない）。拡張子がない場合はnull
	 */
	private static String getFileExtension(String filename) {
		int lastDotIndex = filename.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
			return null;
		}
		return filename.substring(lastDotIndex + 1);
	}

	/**
	 * MIMEタイプとファイル拡張子の整合性をチェックします。
	 * 
	 * @param mimeType MIMEタイプ
	 * @param extension ファイル拡張子（小文字）
	 * @return 整合性がある場合はtrue、それ以外はfalse
	 */
	private static boolean isMimeTypeAndExtensionConsistent(String mimeType, String extension) {
		String lowerExtension = extension.toLowerCase(Locale.ROOT);
		String lowerMimeType = mimeType.toLowerCase(Locale.ROOT);

		return switch (lowerMimeType) {
			case "image/jpeg" -> lowerExtension.equals("jpg") || lowerExtension.equals("jpeg");
			case "image/png" -> lowerExtension.equals("png");
			case "image/gif" -> lowerExtension.equals("gif");
			case "image/webp" -> lowerExtension.equals("webp");
			default -> false;
		};
	}

}
