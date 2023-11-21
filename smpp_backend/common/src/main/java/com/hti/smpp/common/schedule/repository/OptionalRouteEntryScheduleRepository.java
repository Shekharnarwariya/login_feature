package com.hti.smpp.common.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hti.smpp.common.schedule.dto.OptionalRouteEntrySchedule;

public interface OptionalRouteEntryScheduleRepository extends JpaRepository<OptionalRouteEntrySchedule, Integer> {

}
