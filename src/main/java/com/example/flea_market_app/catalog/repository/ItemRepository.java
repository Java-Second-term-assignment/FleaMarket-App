package com.example.flea_market_app.catalog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.catalog.domain.ItemEntity;

public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {

}
