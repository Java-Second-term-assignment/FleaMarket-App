package com.example.flea_market_app.catalog.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class CategoryEntity {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "parent_id")
	private UUID parentId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;
}
