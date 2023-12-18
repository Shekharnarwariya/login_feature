package com.hti.smpp.common.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;

@Service
public interface RouteDAService {

	public Map<Integer, RouteEntryExt> listRouteEntries(int userId, boolean hlr, boolean optional, boolean display);

	public Map<Integer, RouteEntry> getNetworkRouting(int userId);

	public double calculateRoutingCost(int userId, List<String> numbers, int msgParts);

	public double calculateRoutingCost(int userId, Map<String, Integer> numbersParts);
}
