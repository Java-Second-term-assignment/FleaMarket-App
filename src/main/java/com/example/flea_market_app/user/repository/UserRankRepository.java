package com.example.flea_market_app.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.user.domain.UserRank;

public interface UserRankRepository extends JpaRepository<UserRank, Short> {
}
