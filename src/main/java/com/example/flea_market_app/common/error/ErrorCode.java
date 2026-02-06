package com.example.flea_market_app.common.error;

import org.springframework.http.HttpStatus;

/**
 * アプリケーション全体で使用するエラーコードを定義するenum。
 * 
 * <p>各エラーコードは、APIレスポンスで返されるエラーの種類を識別するために使用されます。
 * エラーコードは大文字のスネークケースで命名され、エラーの性質を明確に表現します。
 * 
 * <p>各エラーコードは対応するHTTPステータスコードを持ち、これにより
 * GlobalExceptionHandlerでのマッピングロジックが不要になります。
 * 
 * <p>新しいエラーコードを追加する場合は、以下の点を考慮してください：
 * <ul>
 *   <li>エラーコード名は明確で一意であること</li>
 *   <li>対応するメッセージキーをmessages_ja.propertiesに追加すること</li>
 *   <li>適切なHTTPステータスコードをコンストラクタで指定すること</li>
 * </ul>
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public enum ErrorCode {

	/** ユーザーが見つからない場合のエラーコード */
	USER_NOT_FOUND("error.not_found.user", HttpStatus.NOT_FOUND),

	/** 商品が見つからない場合のエラーコード */
	ITEM_NOT_FOUND("error.not_found.item", HttpStatus.NOT_FOUND),

	/** 注文が見つからない場合のエラーコード */
	ORDER_NOT_FOUND("error.not_found.order", HttpStatus.NOT_FOUND),

	/** メールアドレスの形式が不正な場合のエラーコード */
	INVALID_EMAIL("error.invalid_email", HttpStatus.BAD_REQUEST),

	/** パスワードが不正、またはポリシーに適合しない場合のエラーコード */
	INVALID_PASSWORD("error.password.policy", HttpStatus.BAD_REQUEST),

	/** 商品が既に売却済みの場合のエラーコード */
	LISTING_ALREADY_SOLD("error.listing_already_sold", HttpStatus.BAD_REQUEST),

	/** 決済処理が失敗した場合のエラーコード */
	PAYMENT_FAILED("error.payment_failed", HttpStatus.BAD_REQUEST),

	/** 画像形式が不正な場合のエラーコード */
	INVALID_IMAGE_FORMAT("error.invalid_image_format", HttpStatus.BAD_REQUEST),

	/** 画像サイズが上限超過の場合のエラーコード */
	IMAGE_TOO_LARGE("error.image_too_large", HttpStatus.BAD_REQUEST),

	/** S3への画像アップロードが失敗した場合のエラーコード */
	IMAGE_UPLOAD_FAILED("error.image_upload_failed", HttpStatus.INTERNAL_SERVER_ERROR),

	/** S3からの画像削除が失敗した場合のエラーコード */
	IMAGE_DELETE_FAILED("error.image_delete_failed", HttpStatus.INTERNAL_SERVER_ERROR),

	/** 商品画像数が上限を超えている場合のエラーコード */
	ITEM_IMAGE_COUNT_EXCEEDED("error.item_image_count_exceeded", HttpStatus.BAD_REQUEST),

	/** 商品画像数が最小値を下回っている場合のエラーコード */
	ITEM_IMAGE_COUNT_INSUFFICIENT("error.item_image_count_insufficient", HttpStatus.BAD_REQUEST),

	/** アクセス権限がない場合のエラーコード */
	PERMISSION_DENIED("error.access_denied", HttpStatus.FORBIDDEN),

	/** 認証が必要な場合のエラーコード */
	UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),

	/** 内部サーバーエラーが発生した場合のエラーコード */
	INTERNAL_SERVER_ERROR("error.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR),

	/** 状態遷移が不正な場合のエラーコード（例: PAID以外でconfirmPurchase等） */
	INVALID_STATE("error.invalid_state", HttpStatus.BAD_REQUEST),

	/** 既にお気に入りに追加済みの場合のエラーコード */
	ALREADY_FAVORITED("error.already_favorited", HttpStatus.BAD_REQUEST),

	/** 商品が公開中でない場合のエラーコード（お気に入り追加時など） */
	ITEM_NOT_PUBLISHED("error.item_not_published", HttpStatus.BAD_REQUEST),

	/** 外部サービスの一時障害（ネットワーク/タイムアウト等） */
	EXTERNAL_SERVICE_TEMPORARY("error.external.temporary", HttpStatus.SERVICE_UNAVAILABLE),

	/** 外部サービス呼び出し失敗（外部側エラー等） */
	EXTERNAL_SERVICE_FAILED("error.external.failed", HttpStatus.BAD_GATEWAY),

	/** Webhook の署名検証失敗（不正な署名・改ざん） */
	WEBHOOK_SIGNATURE_INVALID("error.webhook_signature_invalid", HttpStatus.UNAUTHORIZED),

	/** Webhook が未設定（webhook-secret 等が設定されていない） */
	WEBHOOK_NOT_CONFIGURED("error.webhook_not_configured", HttpStatus.SERVICE_UNAVAILABLE),

	/** リソースが見つからない場合の汎用的なエラーコード（未知のリソースタイプ用） */
	RESOURCE_NOT_FOUND("error.not_found", HttpStatus.NOT_FOUND);

	private final String messageKey;
	private final HttpStatus httpStatus;
	private final String apiCode;

	/**
	 * エラーコードのコンストラクタ。
	 * 
	 * @param messageKey このエラーコードに対応するメッセージキー
	 * @param httpStatus このエラーコードに対応するHTTPステータスコード
	 */
	ErrorCode(String messageKey, HttpStatus httpStatus) {
		this(messageKey, httpStatus, null);
	}

	/**
	 * エラーコードのコンストラクタ。
	 * 
	 * @param messageKey このエラーコードに対応するメッセージキー
	 * @param httpStatus このエラーコードに対応するHTTPステータスコード
	 * @param apiCode APIレスポンスで返すエラーコード文字列（nullの場合はenum名を使用）
	 */
	ErrorCode(String messageKey, HttpStatus httpStatus, String apiCode) {
		this.messageKey = messageKey;
		this.httpStatus = httpStatus;
		this.apiCode = apiCode != null ? apiCode : this.name();
	}

	/**
	 * このエラーコードに対応するHTTPステータスコードを取得します。
	 * 
	 * @return HTTPステータスコード
	 */
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}

	/**
	 * このエラーコードに対応するメッセージキーを取得します。
	 * 
	 * <p>メッセージキーは、国際化されたエラーメッセージを取得するために使用されます。
	 * ErrorCodeにmessageKeyを持たせることで、API契約として明確に位置づけられます。
	 * 
	 * @return メッセージキー
	 */
	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * APIレスポンスで返すエラーコード文字列を取得します。
	 * 
	 * <p>このメソッドは、enum名とAPIコードを分離することで、enum名の変更が
	 * APIのbreaking changeにならないようにします。
	 * 
	 * @return APIレスポンスで返すエラーコード文字列
	 */
	public String getApiCode() {
		return apiCode;
	}

	/**
	 * このエラーコードがNOT_FOUND系かどうかを判定します。
	 * 
	 * <p>NOT_FOUND系のエラーコードは、リソースが見つからない場合に使用されます。
	 * このメソッドにより、NotFoundBusinessExceptionで使用可能なErrorCodeを
	 * 型安全に判定できます。
	 * 
	 * @return NOT_FOUND系の場合はtrue、それ以外はfalse
	 */
	public boolean isNotFound() {
		return switch (this) {
		case USER_NOT_FOUND, ITEM_NOT_FOUND, ORDER_NOT_FOUND, RESOURCE_NOT_FOUND -> true;
		default -> false;
		};
	}

}
