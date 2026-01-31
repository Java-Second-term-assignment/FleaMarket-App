package com.example.flea_market_app.admin.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flea_market_app.admin.domain.ReportEntity;

public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
}
