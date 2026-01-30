package com.example.flea_market_app.catalog.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flea_market_app.catalog.domain.ItemEntity;
import com.example.flea_market_app.catalog.repository.ItemRepository;
import com.example.flea_market_app.catalog.service.ItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

	private static final String DRAFT = "DRAFT";
	private static final String JPY = "JPY";

	private final ItemRepository itemRepository;

	@Override
	@Transactional
	public UUID createDraftItem(UUID sellerId, UUID categoryId, String name, String description,
			long priceAmount, String condition, String shippingFeePayer) {
		ItemEntity entity = new ItemEntity();
		entity.setId(UUID.randomUUID());
		entity.setSellerId(sellerId);
		entity.setCategoryId(categoryId);
		entity.setName(name != null ? name : "");
		entity.setDescription(description != null ? description : "");
		entity.setPriceAmount(priceAmount);
		entity.setCurrency(JPY);
		entity.setStatus(DRAFT);
		entity.setCondition(condition != null ? condition : "");
		entity.setShippingFeePayer(shippingFeePayer != null ? shippingFeePayer : "");
		entity.setCopiedFromItemId(null);
		entity.setCopySource(null);
		ItemEntity saved = itemRepository.save(entity);
		return saved.getId();
	}
}
