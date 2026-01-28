package com.example.flea_market_app.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_ranks")
@Getter
@Setter
public class UserRank {

	protected UserRank() {

	}

	@Id
	@Column(name = "id", nullable = false)
	private short id;

	@Column(name = "rank_code", nullable = false)
	private String rankCode;

	@Column(name = "rank_name", nullable = false)
	private String rankName;

	@Column(name = "commission_bps", nullable = false)
	private int commissionBps;
}
