package com.example.flea_market_app.engagement.notification.service.impl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.engagement.notification.domain.NotificationEntity;
import com.example.flea_market_app.engagement.notification.repository.NotificationRepository;
import com.example.flea_market_app.engagement.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final int DEFAULT_LIST_LIMIT = 50;

	private final NotificationRepository notificationRepository;

	@Override
	@Transactional
	public void create(UUID userId, String message) {
		NotificationEntity entity = new NotificationEntity();
		entity.setId(UUID.randomUUID());
		entity.setUserId(userId);
		entity.setMessage(message != null ? message : "");
		entity.setCreatedAt(OffsetDateTime.now());
		notificationRepository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<NotificationEntity> listByUser(UUID userId, int limit) {
		int size = limit > 0 ? Math.min(limit, 100) : DEFAULT_LIST_LIMIT;
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, size));
	}
}
