package com.hti.smpp.common.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.report.dto.PerformResult;

@Repository
public interface PerformResultRepository extends JpaRepository<PerformResult, Integer> {

}
