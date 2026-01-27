package com.example.flea_market_app.user.repository.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
}
