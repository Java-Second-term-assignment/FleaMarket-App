package com.example.flea_market_app.admin.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.admin.domain.ReportEntity;
import com.example.flea_market_app.admin.domain.ReportType;
import com.example.flea_market_app.admin.domain.TargetType;
import com.example.flea_market_app.admin.repository.ReportRepository;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

import lombok.RequiredArgsConstructor;

/**
 * 通報を登録するサービス。商品・ユーザー等をターゲットに通報できる。
 */
@Service
@RequiredArgsConstructor
public class ReportService {

	private final ReportRepository reportRepository;
	private final ItemRepository itemRepository;

	private static final int DESCRIPTION_MAX_LENGTH = 2000;

	/**
	 * 通報を登録する。
	 *
	 * @param reporterId  通報者（ログイン中ユーザー）のID
	 * @param targetType  ターゲット種別（ITEM, USER 等）
	 * @param targetId   ターゲットのID
	 * @param reportType  通報理由（COUNTERFEIT, HARASSMENT 等）
	 * @param description 詳細（任意、最大2000文字）
	 */
	@Transactional
	public void submitReport(UUID reporterId, TargetType targetType, UUID targetId,
			ReportType reportType, String description) {
		if (targetType == TargetType.ITEM) {
			if (!itemRepository.existsById(targetId)) {
				throw com.example.flea_market_app.common.exception.NotFoundBusinessException
						.of(com.example.flea_market_app.common.exception.ResourceType.ITEM);
			}
		}
		String desc = description != null ? description.trim() : "";
		if (desc.length() > DESCRIPTION_MAX_LENGTH) {
			throw new ValidationBusinessException(
					com.example.flea_market_app.common.error.ErrorCode.INTERNAL_SERVER_ERROR,
					"詳細は" + DESCRIPTION_MAX_LENGTH + "文字以内で入力してください。");
		}

		ReportEntity e = new ReportEntity();
		e.setId(UUID.randomUUID());
		e.setReporterId(reporterId);
		e.setTargetType(targetType.name());
		e.setTargetId(targetId);
		e.setReportType(reportType.name());
		e.setDescription(desc.isEmpty() ? null : desc);
		e.setCreatedAt(OffsetDateTime.now());
		reportRepository.save(e);
	}
}
