package com.hti.smpp.common.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.HlrRouteEntry;

@Repository
public interface HlrRouteEntryRepository extends JpaRepository<HlrRouteEntry, Integer> {

	public HlrRouteEntry findByRouteId(int id);

}
