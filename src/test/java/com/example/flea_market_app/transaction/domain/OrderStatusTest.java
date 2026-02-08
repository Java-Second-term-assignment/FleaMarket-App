package com.example.flea_market_app.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

	@Test
	void fromString_validValues_returnsEnum() {
		assertThat(OrderStatus.fromString("PAID")).isEqualTo(OrderStatus.PAID);
		assertThat(OrderStatus.fromString("CANCELLED")).isEqualTo(OrderStatus.CANCELLED);
		assertThat(OrderStatus.fromString("AWAITING_SHIPMENT")).isEqualTo(OrderStatus.AWAITING_SHIPMENT);
		assertThat(OrderStatus.fromString("SHIPPED")).isEqualTo(OrderStatus.SHIPPED);
		assertThat(OrderStatus.fromString("COMPLETED")).isEqualTo(OrderStatus.COMPLETED);
	}

	@Test
	void fromString_null_throwsIllegalStateException() {
		assertThatThrownBy(() -> OrderStatus.fromString(null))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("null or blank");
	}

	@Test
	void fromString_blank_throwsIllegalStateException() {
		assertThatThrownBy(() -> OrderStatus.fromString(""))
				.isInstanceOf(IllegalStateException.class);
		assertThatThrownBy(() -> OrderStatus.fromString("   "))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void fromString_invalidValue_throwsIllegalStateException() {
		assertThatThrownBy(() -> OrderStatus.fromString("INVALID"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("invalid");
	}
}
