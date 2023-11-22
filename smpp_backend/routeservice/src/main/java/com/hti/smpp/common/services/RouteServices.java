package com.hti.smpp.common.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.OptEntryArrForm;
import com.hti.smpp.common.request.RouteRequest;
import com.hti.smpp.common.request.SearchCriteria;
import com.hti.smpp.common.route.dto.HlrEntryLog;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalEntryLog;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.dto.RouteEntryLog;

@Service
public interface RouteServices {

	public String saveRoute(RouteRequest RouteRequest, String username);

	public String updateOptionalRoute(OptEntryArrForm optEntryArrForm, String username);

	public void saveRouteEntry(RouteEntryExt entry);

	public void saveDefaultEntries(RouteEntry entry);

	public void deleteRouteEntries(int userId);

	public void deleteRouteEntries(int userId, Set<Integer> networks);

	public void deleteRouteEntries(List<RouteEntry> list);

	public void updateRouteEntries(List<RouteEntry> list);

	public void updateOptionalRouteEntries(List<OptionalRouteEntry> list);

	public void updateHlrRouteEntries(List<HlrRouteEntry> list);

	public void saveEntries(List<RouteEntryExt> list);

	public Map<Integer, RouteEntryExt> listRouteEntries(int userId, boolean hlr, boolean optional, boolean display);

	public Map<Integer, RouteEntryExt> listRouteEntries(SearchCriteria searchCriteria);

	// public Map<Integer, RouteEntryExt> getNetworkRouting(int userId, boolean
	// hlr);

	public Map<Integer, RouteEntry> getNetworkRouting(int userId);

	public Map<Integer, RouteEntryExt> listCoverage(int userId, boolean display, boolean cached);

	public Map<Integer, RouteEntryExt> listCoverage(String systemId, boolean display, boolean cached);

	public Map<Integer, RouteEntryLog> listBasicLogEntries(int[] routeId);

	public Map<Integer, HlrEntryLog> listHlrLog(int[] routeId);

	public Map<Integer, OptionalEntryLog> listOptLog(int[] routeId);

	public Set<String> distinctSmscTypes();

	public double calculateLookupCost(int userId, List<String> numbers);

	public double calculateRoutingCost(int userId, List<String> numbers, int msgParts);

	public double calculateRoutingCost(int userId, Map<String, Integer> numbersParts);

}
