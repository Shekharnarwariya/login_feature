package com.hti.smpp.common.route.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hti.smpp.common.route.dto.RouteEntry;

@Repository
public interface RouteEntryRepository extends JpaRepository<RouteEntry, Integer> {

	public List<RouteEntry> findAll(Specification<RouteEntry> spec);

	public List<RouteEntry> findByUserId(int userId);

	public void deleteByUserId(int userId);

	public void deleteByUserIdAndNetworkIdIn(int userId, ArrayList arrayList);

	public List<RouteEntry> findByUserIdInAndSmscIdInAndGroupIdInAndNetworkIdInAndSmscTypeInAndCostBetween(
			int[] userIds, int[] smscIds, int[] groupIds, int[] networkIds, String[] smscTypes, double minCost,
			double maxCost);

}
