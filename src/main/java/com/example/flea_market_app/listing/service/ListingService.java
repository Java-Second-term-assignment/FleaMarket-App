package com.example.flea_market_app.listing.service;

import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.example.flea_market_app.listing.service.dto.CreateItemRequest;
import com.example.flea_market_app.listing.service.dto.CreateItemResponse;

/**
 * 商品出品に関するサービスのインターフェース。
 * 
 * @author FleaMarket-App Team
 * @since 1.0.0
 */
public interface ListingService {

	/**
	 * 商品を作成し、画像をアップロードします。
	 * 
	 * <p>商品はDRAFT状態で作成されます。
	 * 画像は1-10枚の範囲でアップロード可能です。
	 * 
	 * @param sellerId 出品者ID
	 * @param request 商品作成リクエスト
	 * @param images アップロードする画像ファイルのリスト（nullまたは空の場合は画像なしで作成）
	 * @return 商品作成レスポンス（商品ID、画像URLリスト）
	 */
	CreateItemResponse createItem(UUID sellerId, CreateItemRequest request, List<MultipartFile> images);
}
