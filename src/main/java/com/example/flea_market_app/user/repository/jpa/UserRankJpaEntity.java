package com.example.flea_market_app.user.repository.jpa;

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
public class UserRankJpaEntity {

	@Id
	@Column(name = "id", nullable = false)
	private short id;

	@Column(name = "rank_code", nullable = false, unique = true)
	private String code;

	@Column(name = "rank_name", nullable = false)
	private String name;

	@Column(name = "commission_bps", nullable = false)
	private int commissionBps;

}
