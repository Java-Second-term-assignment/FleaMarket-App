package com.example.flea_market_app.common.exception;

import com.example.flea_market_app.common.error.ErrorCode;

/**
 * リソースタイプを定義するenum。
 * 
 * <p>このenumは、NotFoundBusinessExceptionで使用されるリソースタイプを
 * 型安全に定義します。各リソースタイプは対応するErrorCodeを持ちます。
 * 
 * <p>messageKeyはErrorCodeに集約されているため、このenumは純粋な分類enumとして
 * 機能します。これにより、ドメイン層からUI都合（i18n）を分離できます。
 * 
 * <p>新しいリソースタイプを追加する場合は、対応するErrorCodeも追加してください。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public enum ResourceType {

	/** ユーザーリソース */
	USER(ErrorCode.USER_NOT_FOUND),

	/** 商品リソース */
	ITEM(ErrorCode.ITEM_NOT_FOUND),

	/** 注文リソース */
	ORDER(ErrorCode.ORDER_NOT_FOUND),

	/** ランクリソース */
	RANK(ErrorCode.RANK_NOT_FOUND);

	private final ErrorCode errorCode;

	/**
	 * リソースタイプのコンストラクタ。
	 * 
	 * @param errorCode このリソースタイプに対応するErrorCode
	 */
	ResourceType(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * このリソースタイプに対応するErrorCodeを取得します。
	 * 
	 * @return ErrorCode
	 */
	public ErrorCode getErrorCode() {
		return errorCode;
	}

}
