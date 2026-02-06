package com.example.flea_market_app.listing.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ListingTest {

	private static final UUID SELLER_ID = UUID.randomUUID();
	private static final UUID CATEGORY_ID = UUID.randomUUID();

	@Nested
	class CreateDraft {
		@Test
		void validInput_returnsDraft() {
			Listing draft = Listing.createDraft(
					SELLER_ID,
					CATEGORY_ID,
					"Item name",
					"Description",
					1000L,
					ItemCondition.USED_GOOD,
					ShippingFeePayer.BUYER);
			assertThat(draft).isNotNull();
			assertThat(draft.getName()).isEqualTo("Item name");
			assertThat(draft.getDescription()).isEqualTo("Description");
			assertThat(draft.getPriceAmount()).isEqualTo(1000L);
			assertThat(draft.getStatus()).isEqualTo(ItemStatus.DRAFT);
		}

		@Test
		void nameNull_throwsIllegalArgumentException() {
			assertThatThrownBy(() -> Listing.createDraft(
					SELLER_ID, CATEGORY_ID, null, "desc", 0L, ItemCondition.USED_GOOD, ShippingFeePayer.BUYER))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("name");
		}

		@Test
		void nameBlank_throwsIllegalArgumentException() {
			assertThatThrownBy(() -> Listing.createDraft(
					SELLER_ID, CATEGORY_ID, "  ", "desc", 0L, ItemCondition.USED_GOOD, ShippingFeePayer.BUYER))
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void nameOver200Chars_throwsIllegalArgumentException() {
			String longName = "a".repeat(201);
			assertThatThrownBy(() -> Listing.createDraft(
					SELLER_ID, CATEGORY_ID, longName, "desc", 0L, ItemCondition.USED_GOOD, ShippingFeePayer.BUYER))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("200");
		}

		@Test
		void descriptionOver5000Chars_throwsIllegalArgumentException() {
			String longDesc = "a".repeat(5001);
			assertThatThrownBy(() -> Listing.createDraft(
					SELLER_ID, CATEGORY_ID, "name", longDesc, 0L, ItemCondition.USED_GOOD, ShippingFeePayer.BUYER))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("5000");
		}

		@Test
		void priceAmountNegative_throwsIllegalArgumentException() {
			assertThatThrownBy(() -> Listing.createDraft(
					SELLER_ID, CATEGORY_ID, "name", "desc", -1L, ItemCondition.USED_GOOD, ShippingFeePayer.BUYER))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("priceAmount");
		}
	}
}
