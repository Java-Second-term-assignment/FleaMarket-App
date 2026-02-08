package com.example.flea_market_app.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.flea_market_app.common.exception.AccessDeniedBusinessException;
import com.example.flea_market_app.common.exception.ValidationBusinessException;

class OrderTest {

	private static final UUID ORDER_ID = UUID.randomUUID();
	private static final UUID ITEM_ID = UUID.randomUUID();
	private static final UUID BUYER_ID = UUID.randomUUID();
	private static final UUID SELLER_ID = UUID.randomUUID();
	private static final UUID OTHER_ID = UUID.randomUUID();

	private Order order;

	@BeforeEach
	void setUp() {
		order = new Order(ORDER_ID, ITEM_ID, BUYER_ID, SELLER_ID, OrderStatus.PAID);
	}

	@Nested
	class AssertBuyer {
		@Test
		void whenBuyer_doesNotThrow() {
			order.assertBuyer(BUYER_ID);
		}

		@Test
		void whenNotBuyer_throwsAccessDenied() {
			assertThatThrownBy(() -> order.assertBuyer(SELLER_ID))
					.isInstanceOf(AccessDeniedBusinessException.class);
			assertThatThrownBy(() -> order.assertBuyer(OTHER_ID))
					.isInstanceOf(AccessDeniedBusinessException.class);
		}

		@Test
		void whenUserIdNull_throwsNPE() {
			assertThatThrownBy(() -> order.assertBuyer(null))
					.isInstanceOf(NullPointerException.class);
		}
	}

	@Nested
	class AssertSeller {
		@Test
		void whenSeller_doesNotThrow() {
			order.assertSeller(SELLER_ID);
		}

		@Test
		void whenNotSeller_throwsAccessDenied() {
			assertThatThrownBy(() -> order.assertSeller(BUYER_ID))
					.isInstanceOf(AccessDeniedBusinessException.class);
		}
	}

	@Nested
	class AssertParticipant {
		@Test
		void whenBuyer_doesNotThrow() {
			order.assertParticipant(BUYER_ID);
		}

		@Test
		void whenSeller_doesNotThrow() {
			order.assertParticipant(SELLER_ID);
		}

		@Test
		void whenNeither_throwsAccessDenied() {
			assertThatThrownBy(() -> order.assertParticipant(OTHER_ID))
					.isInstanceOf(AccessDeniedBusinessException.class);
		}
	}

	@Nested
	class ConfirmPurchase {
		@Test
		void whenPaid_transitionsToAwaitingShipment() {
			order.confirmPurchase();
			assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_SHIPMENT);
		}

		@Test
		void whenNotPaid_throwsValidationBusinessException() {
			Order shipped = new Order(ORDER_ID, ITEM_ID, BUYER_ID, SELLER_ID, OrderStatus.SHIPPED);
			assertThatThrownBy(shipped::confirmPurchase)
					.isInstanceOf(ValidationBusinessException.class);
		}
	}

	@Nested
	class Ship {
		@Test
		void whenAwaitingShipment_setsShipped() {
			order.confirmPurchase();
			OffsetDateTime now = OffsetDateTime.now();
			order.ship(now);
			assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
			assertThat(order.getShippedAt()).isEqualTo(now);
		}

		@Test
		void whenNotAwaitingShipment_throwsValidationBusinessException() {
			OffsetDateTime now = OffsetDateTime.now();
			assertThatThrownBy(() -> order.ship(now))
					.isInstanceOf(ValidationBusinessException.class);
		}
	}

	@Nested
	class Complete {
		@Test
		void whenShipped_transitionsToCompleted() {
			order.confirmPurchase();
			OffsetDateTime now = OffsetDateTime.now();
			order.ship(now);
			OffsetDateTime completedAt = OffsetDateTime.now();
			order.complete(completedAt);
			assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
			assertThat(order.getCompletedAt()).isEqualTo(completedAt);
		}

		@Test
		void whenNotShipped_throwsValidationBusinessException() {
			assertThatThrownBy(() -> order.complete(OffsetDateTime.now()))
					.isInstanceOf(ValidationBusinessException.class);
		}
	}

	@Nested
	class Cancel {
		@Test
		void whenPaid_transitionsToCancelled() {
			order.cancel();
			assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
		}

		@Test
		void whenShipped_throwsValidationBusinessException() {
			order.confirmPurchase();
			order.ship(OffsetDateTime.now());
			assertThatThrownBy(order::cancel)
					.isInstanceOf(ValidationBusinessException.class);
		}

		@Test
		void whenCompleted_throwsValidationBusinessException() {
			Order completed = new Order(ORDER_ID, ITEM_ID, BUYER_ID, SELLER_ID, OrderStatus.COMPLETED);
			assertThatThrownBy(completed::cancel)
					.isInstanceOf(ValidationBusinessException.class);
		}
	}
}
