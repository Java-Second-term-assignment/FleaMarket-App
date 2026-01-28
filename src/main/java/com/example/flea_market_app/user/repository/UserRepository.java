package com.example.flea_market_app.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.user.domain.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

}
