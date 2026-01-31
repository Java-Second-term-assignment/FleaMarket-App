package com.example.flea_market_app.admin.service;

import org.springframework.stereotype.Service;

/**
 * AIによる一次判定（補助）。
 *
 * 原則:
 * - AI は決定権を持たない
 * - 判定結果は admin / ルールエンジンが最終判断する
 *
 * 将来:
 * - integration/aws (Comprehend/Rekognition) を呼び出す
 */
@Service
public class AiModerationService {
	// TODO: implement later
}