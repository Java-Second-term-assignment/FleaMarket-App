package com.example.flea_market_app.transaction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.repository.OrderMessageRepository;
import com.example.flea_market_app.transaction.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderMessageRepository orderMessageRepository;

	@Mock
	private EmailNotificationSender emailNotificationSender;

	@InjectMocks
	private ChatService chatService;

	@Test
	void listMessages_whenOrderNotFound_throwsNotFoundBusinessException() {
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> chatService.listMessages(orderId, userId, 0, 50))
				.isInstanceOf(NotFoundBusinessException.class);
	}

	@Test
	void listMessages_whenUserNotParticipant_throwsAccessDeniedBusinessException() {
		UUID orderId = UUID.randomUUID();
		UUID buyerId = UUID.randomUUID();
		UUID sellerId = UUID.randomUUID();
		UUID otherUserId = UUID.randomUUID();

		OrderEntity order = new OrderEntity();
		order.setId(orderId);
		order.setBuyerId(buyerId);
		order.setSellerId(sellerId);
		order.setStatus(OrderStatus.PAID.name());

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

		assertThatThrownBy(() -> chatService.listMessages(orderId, otherUserId, 0, 50))
				.isInstanceOf(AccessDeniedBusinessException.class);
	}

	@Test
	void listMessages_whenParticipant_returnsList() {
		UUID orderId = UUID.randomUUID();
		UUID buyerId = UUID.randomUUID();
		UUID sellerId = UUID.randomUUID();

		OrderEntity order = new OrderEntity();
		order.setId(orderId);
		order.setBuyerId(buyerId);
		order.setSellerId(sellerId);
		order.setStatus(OrderStatus.PAID.name());

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(orderMessageRepository.findByOrderIdOrderByCreatedAtAsc(any(), any())).thenReturn(java.util.List.of());

		var result = chatService.listMessages(orderId, buyerId, 0, 50);

		assertThat(result).isEmpty();
		verify(orderMessageRepository).findByOrderIdOrderByCreatedAtAsc(any(), any());
	}

	@Test
	void sendMessage_whenOrderNotFound_throwsNotFoundBusinessException() {
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> chatService.sendMessage(orderId, userId, "Hello"))
				.isInstanceOf(NotFoundBusinessException.class);
	}

	@Test
	void sendMessage_whenParticipant_succeeds() {
		UUID orderId = UUID.randomUUID();
		UUID buyerId = UUID.randomUUID();
		UUID sellerId = UUID.randomUUID();

		OrderEntity order = new OrderEntity();
		order.setId(orderId);
		order.setBuyerId(buyerId);
		order.setSellerId(sellerId);
		order.setStatus(OrderStatus.PAID.name());

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
		when(orderMessageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		chatService.sendMessage(orderId, buyerId, "Hello");

		verify(orderMessageRepository).save(any());
	}
}
