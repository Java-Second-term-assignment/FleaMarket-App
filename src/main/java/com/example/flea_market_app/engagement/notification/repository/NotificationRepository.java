package com.example.flea_market_app.engagement.notification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.engagement.notification.domain.NotificationEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

	List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
