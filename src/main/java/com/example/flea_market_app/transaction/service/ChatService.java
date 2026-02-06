package com.example.flea_market_app.transaction.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;
import com.example.flea_market_app.transaction.domain.ChatMessage;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderMessageEntity;
import com.example.flea_market_app.transaction.repository.OrderMessageRepository;
import com.example.flea_market_app.transaction.repository.OrderRepository;
import com.example.flea_market_app.transaction.service.dto.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final OrderRepository orderRepository;
	private final OrderMessageRepository orderMessageRepository;
	private final EmailNotificationSender emailNotificationSender;

	@Transactional
	public void sendMessage(UUID orderId, UUID currentUserId, String content) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));
		assertParticipant(order, currentUserId);

		ChatMessage msg = ChatMessage.user(orderId, currentUserId, content, OffsetDateTime.now());

		OrderMessageEntity e = new OrderMessageEntity();
		e.setId(msg.getId());
		e.setOrderId(msg.getOrderId());
		e.setSenderId(msg.getSenderId());
		e.setContent(msg.getContent());
		e.setTemplate(msg.isTemplate());
		e.setCreatedAt(msg.getCreatedAt());

		orderMessageRepository.save(e);

		UUID recipientUserId = order.getBuyerId().equals(currentUserId) ? order.getSellerId() : order.getBuyerId();
		emailNotificationSender.sendChatReceived(recipientUserId, orderId);
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> listMessages(UUID orderId, UUID currentUserId, int page, int size) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));

		assertParticipant(order, currentUserId);

		int safeSize = Math.max(1, Math.min(size, 100));
		List<OrderMessageEntity> entities = orderMessageRepository.findByOrderIdOrderByCreatedAtAsc(
				orderId, PageRequest.of(page, safeSize));
		return entities.stream().map(ChatMessageResponse::from).collect(Collectors.toList());
	}

	private void assertParticipant(OrderEntity order, UUID userId) {
		boolean participant = order.getBuyerId().equals(userId) || order.getSellerId().equals(userId);
		if (!participant) {
			throw new AccessDeniedBusinessException(); // ← 引数なしに修正
		}
	}
}
