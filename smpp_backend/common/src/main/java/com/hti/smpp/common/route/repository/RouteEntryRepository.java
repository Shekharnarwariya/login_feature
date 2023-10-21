package com.hti.smpp.common.route.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.RouteEntry;

@Repository
public interface RouteEntryRepository extends JpaRepository<RouteEntry, Integer> {

	public List<RouteEntry> findAll(Specification<RouteEntry> spec);

	public List<RouteEntry> findByUserId(int userId);

}
