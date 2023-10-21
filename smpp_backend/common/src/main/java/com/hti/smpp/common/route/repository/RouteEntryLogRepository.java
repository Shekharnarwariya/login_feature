package com.hti.smpp.common.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.RouteEntryLog;

@Repository
public interface RouteEntryLogRepository extends JpaRepository<RouteEntryLog, Integer> {

}
