package com.hti.smpp.common.route.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.OptionalRouteEntry;

@Repository
public interface OptionalRouteEntryRepository extends JpaRepository<OptionalRouteEntry, Integer> {

}
