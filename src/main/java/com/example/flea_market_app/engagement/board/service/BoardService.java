package com.example.flea_market_app.engagement.board.service;

import java.util.List;
import java.util.UUID;

import com.example.flea_market_app.engagement.board.service.dto.PostResponse;

public interface BoardService {

	/**
	 * 掲示板の投稿一覧を取得する（最新順、ページネーション付き）。
	 *
	 * @param itemId 商品ID（board_id）
	 * @param page   ページ番号（0 始まり）
	 * @param size   1 ページあたりの件数（1〜100）
	 * @return 投稿一覧
	 */
	List<PostResponse> getPosts(UUID itemId, int page, int size);

	/**
	 * 掲示板に投稿する。公開中（PUBLISHED）の商品の掲示板にのみ投稿可能。
	 *
	 * @param itemId   商品ID（board_id）
	 * @param authorId 投稿者ユーザーID
	 * @param content  本文
	 * @return 作成された投稿のID（Location ヘッダ用）
	 */
	UUID createPost(UUID itemId, UUID authorId, String content);
}
