package com.example.flea_market_app.transaction.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.common.exception.ResourceType; // ← スクショにある前提
import com.example.flea_market_app.transaction.domain.ChatMessage;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderMessageEntity;
import com.example.flea_market_app.transaction.repository.OrderMessageRepository;
import com.example.flea_market_app.transaction.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

	private final OrderRepository orderRepository;
	private final OrderMessageRepository orderMessageRepository;

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
	}

	@Transactional(readOnly = true)
	public List<OrderMessageEntity> listMessages(UUID orderId, UUID currentUserId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> NotFoundBusinessException.of(ResourceType.ORDER));

		assertParticipant(order, currentUserId);

		return orderMessageRepository.findByOrderIdOrderByCreatedAtAsc(orderId);
	}

	private void assertParticipant(OrderEntity order, UUID userId) {
		boolean participant = order.getBuyerId().equals(userId) || order.getSellerId().equals(userId);
		if (!participant) {
			throw new AccessDeniedBusinessException(); // ← 引数なしに修正
		}
	}
}
