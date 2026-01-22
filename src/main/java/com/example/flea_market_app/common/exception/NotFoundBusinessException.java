package com.example.flea_market_app.common.exception;

import java.util.Objects;

import com.example.flea_market_app.common.error.ErrorCode;

/**
 * リソースが見つからない場合にスローされるビジネス例外。
 * 
 * <p>この例外は、指定されたリソース（ユーザー、商品、注文など）が
 * データベースまたはシステム内に存在しない場合に使用されます。
 * 
 * <p>使用例：
 * <pre>{@code
 * // リソースタイプを指定する場合（推奨）
 * throw NotFoundBusinessException.of(ResourceType.USER);
 * 
 * // ErrorCodeを直接指定する場合（ResourceTypeに当てはまらないケース）
 * throw NotFoundBusinessException.of(ErrorCode.RESOURCE_NOT_FOUND);
 * }</pre>
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public class NotFoundBusinessException extends BusinessException {

	/**
	 * デフォルトコンストラクタ。
	 * 
	 * <p>リソースタイプが指定されていない場合に使用されます。
	 * ErrorCode.RESOURCE_NOT_FOUNDが設定されます。
	 */
	private NotFoundBusinessException() {
		super(
				ErrorCode.RESOURCE_NOT_FOUND,
				ErrorCode.RESOURCE_NOT_FOUND.getMessageKey()
		);
	}

	/**
	 * ErrorCodeを直接指定するコンストラクタ。
	 * 
	 * <p>このコンストラクタにより、ResourceTypeに当てはまらないケース
	 * （検索条件が不正、外部IDが無効など）にも対応できます。
	 * enumに閉じない設計により、将来の例外増殖に耐えることができます。
	 * 
	 * @param errorCode エラーコード
	 * @throws NullPointerException errorCodeがnullの場合
	 */
	private NotFoundBusinessException(ErrorCode errorCode) {
		super(
				Objects.requireNonNull(errorCode, "errorCode must not be null"),
				errorCode.getMessageKey()
		);
	}

	/**
	 * リソースタイプを指定して例外を作成します。
	 * 
	 * <p>ResourceType enumを使用することで、型安全性が保証され、
	 * typoを防ぐことができます。
	 * 
	 * @param resourceType リソースタイプ（ResourceType.USER、ResourceType.ITEMなど）
	 * @return NotFoundBusinessExceptionインスタンス
	 * @throws NullPointerException resourceTypeがnullの場合
	 */
	public static NotFoundBusinessException of(ResourceType resourceType) {
		Objects.requireNonNull(resourceType, "resourceType must not be null");
		return new NotFoundBusinessException(resourceType.getErrorCode());
	}

	/**
	 * ErrorCodeを直接指定して例外を作成します。
	 * 
	 * <p>このメソッドにより、ResourceTypeに当てはまらないケース
	 * （検索条件が不正、外部IDが無効など）にも対応できます。
	 * enumに閉じない設計により、将来の例外増殖に耐えることができます。
	 * 
	 * <p>このメソッドはNOT_FOUND系のErrorCodeのみを受け付けます。
	 * それ以外のErrorCodeを指定した場合はIllegalArgumentExceptionがスローされます。
	 * 
	 * @param errorCode エラーコード（NOT_FOUND系である必要がある）
	 * @return NotFoundBusinessExceptionインスタンス
	 * @throws NullPointerException errorCodeがnullの場合
	 * @throws IllegalArgumentException errorCodeがNOT_FOUND系でない場合
	 */
	public static NotFoundBusinessException of(ErrorCode errorCode) {
		Objects.requireNonNull(errorCode, "errorCode must not be null");
		if (!errorCode.isNotFound()) {
			throw new IllegalArgumentException(
					"ErrorCode must be NOT_FOUND type, but got: " + errorCode);
		}
		return new NotFoundBusinessException(errorCode);
	}

	/**
	 * 汎用的なNotFound例外を作成します。
	 * 
	 * <p><strong>注意:</strong> このメソッドは例外的な用途でのみ使用してください。
	 * 通常は、リソースタイプが特定できる場合は{@link #of(ResourceType)}を使用し、
	 * ResourceTypeに当てはまらないがErrorCodeが特定できる場合は{@link #of(ErrorCode)}を使用してください。
	 * 
	 * <p>このメソッドを使用すると、ログや分析の粒度が落ち、デバッグが困難になります。
	 * リソースタイプが特定できない場合のみ、このメソッドを使用してください。
	 * 
	 * <p>ErrorCode.RESOURCE_NOT_FOUNDが設定されます。
	 * 
	 * @return NotFoundBusinessExceptionインスタンス
	 */
	static NotFoundBusinessException generic() {
		return new NotFoundBusinessException();
	}

}
