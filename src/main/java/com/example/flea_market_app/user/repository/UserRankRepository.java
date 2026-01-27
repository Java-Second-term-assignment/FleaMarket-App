package com.example.flea_market_app.user.repository;

import com.example.flea_market_app.user.domain.UserRank;

// UserRankの
public interface UserRankRepository {

	UserRank getById(short id);
}
