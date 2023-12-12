package com.hti.smpp.common.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.schedule.dto.HlrRouteEntrySch;

@Repository
public interface HlrRouteEntrySchRepository extends JpaRepository<HlrRouteEntrySch, Integer> {

	public HlrRouteEntrySch findByScheduleOn(String scheduledOn);

}
