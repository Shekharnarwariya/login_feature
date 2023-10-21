package com.hti.smpp.common.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.schedule.dto.ScheduleEntry;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Integer> {

}
