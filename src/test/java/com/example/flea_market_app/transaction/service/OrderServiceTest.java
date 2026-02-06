package com.example.flea_market_app.transaction.service;

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

import com.example.flea_market_app.common.exception.NotFoundBusinessException;
import com.example.flea_market_app.engagement.notification.service.EmailNotificationSender;
import com.example.flea_market_app.engagement.notification.service.NotificationService;
import com.example.flea_market_app.transaction.domain.OrderEntity;
import com.example.flea_market_app.transaction.domain.OrderStatus;
import com.example.flea_market_app.transaction.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private com.example.flea_market_app.catalog.repository.ItemRepository itemRepository;

	@Mock
	private EmailNotificationSender emailNotificationSender;

	@Mock
	private NotificationService notificationService;

	@InjectMocks
	private OrderService orderService;

	@Test
	void confirmPurchase_whenOrderNotFound_throwsNotFoundBusinessException() {
		UUID orderId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> orderService.confirmPurchase(orderId, userId))
				.isInstanceOf(NotFoundBusinessException.class);

		verify(orderRepository).findById(orderId);
	}

	@Test
	void confirmPurchase_whenOrderExistsAndUserIsBuyer_succeeds() {
		UUID orderId = UUID.randomUUID();
		UUID buyerId = UUID.randomUUID();
		UUID sellerId = UUID.randomUUID();
		UUID itemId = UUID.randomUUID();

		OrderEntity entity = new OrderEntity();
		entity.setId(orderId);
		entity.setItemId(itemId);
		entity.setBuyerId(buyerId);
		entity.setSellerId(sellerId);
		entity.setStatus(OrderStatus.PAID.name());
		entity.setAppliedCommissionBps(0);
		entity.setItemPriceAmount(1000L);
		entity.setShippingFeeAmount(0L);
		entity.setTotalAmount(1000L);
		entity.setCurrency("JPY");
		entity.setShippingAddressSnapshot("{}");

		when(orderRepository.findById(orderId)).thenReturn(Optional.of(entity));
		when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		orderService.confirmPurchase(orderId, buyerId);

		verify(orderRepository).save(any(OrderEntity.class));
	}
}
