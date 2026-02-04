package com.example.flea_market_app.admin.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.ItemModerationEntity;
import com.example.flea_market_app.admin.repository.ItemModerationRepository;
import com.example.flea_market_app.integration.aws.dto.ImageModerationResult;
import com.example.flea_market_app.integration.aws.dto.ModerationResult;
import com.example.flea_market_app.listing.service.ListingModerationService.ModerationDecision;

import lombok.RequiredArgsConstructor;

/**
 * AIモデレーション判定結果のうち、違反と判定されたものだけをDBに記録する。
 */
@Service
@RequiredArgsConstructor
public class ItemModerationRecordService {

	private final ItemModerationRepository itemModerationRepository;

	/**
	 * 判定結果が違反（reject=true）の場合のみ、item_moderation に保存する。
	 */
	@Transactional
	public void recordIfRejected(UUID itemId, ModerationDecision decision) {
		if (!decision.reject()) {
			return;
		}
		ItemModerationEntity entity = new ItemModerationEntity();
		entity.setId(UUID.randomUUID());
		entity.setItemId(itemId);
		entity.setRejected(true);

		if (decision.textResult() instanceof ModerationResult text) {
			entity.setTextFlagged(text.flagged());
			entity.setTextNegativeScore(text.negativeScore());
		}
		if (decision.imageResult() instanceof ImageModerationResult img) {
			entity.setImageAdult(img.adult());
			entity.setImageViolence(img.violence());
			entity.setImageRiskScore(img.riskScore());
		}

		itemModerationRepository.save(entity);
	}
}
