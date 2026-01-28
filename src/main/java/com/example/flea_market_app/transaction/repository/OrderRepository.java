package com.example.flea_market_app.transaction.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.transaction.domain.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
