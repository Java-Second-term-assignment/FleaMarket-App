package com.example.flea_market_app.user.domain;

import java.time.OffsetDateTime;

import lombok.Value;

@Value
public class UserRank {
	
	short id; // user_ranks.idを参照
	String code;
	String name;
	int commissionBps;
	OffsetDateTime evaluatedAt;

}
