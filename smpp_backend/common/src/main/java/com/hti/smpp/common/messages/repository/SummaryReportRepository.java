package com.hti.smpp.common.messages.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.messages.dto.SummaryReport;

public interface SummaryReportRepository extends JpaRepository<SummaryReport, Integer> {
}
