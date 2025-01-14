package com.hti.smpp.common.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.request.HlrEntryArrForm;
import com.hti.smpp.common.request.OptEntryArrForm;
import com.hti.smpp.common.request.RouteEntryArrForm;
import com.hti.smpp.common.request.RouteRequest;
import com.hti.smpp.common.request.SearchCriteria;
import com.hti.smpp.common.response.OptionRouteResponse;
import com.hti.smpp.common.response.RouteUserResponse;
import com.hti.smpp.common.route.dto.HlrEntryLog;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalEntryLog;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.dto.RouteEntryLog;

import jakarta.servlet.http.HttpServletResponse;
/**
 * The RouteServices interface defines the contract for handling route-related operations.
 */
@Service
public interface RouteServices {

	public String saveRoute(RouteRequest RouteRequest, String username);

	public OptionRouteResponse updateOptionalRoute(OptEntryArrForm optEntryArrForm, String username);

	public OptionRouteResponse UpdateOptionalRouteUndo(OptEntryArrForm optEntryArrForm, String username);

	public OptionRouteResponse UpdateOptionalRoutePrevious(OptEntryArrForm optEntryArrForm, String username);

	public OptionRouteResponse UpdateOptionalRouteBasic(OptEntryArrForm optEntryArrForm, String username);

	public OptionRouteResponse UpdateOptionalRouteHlr(OptEntryArrForm optEntryArrForm, String username);

	public OptionRouteResponse checkExisting(RouteEntryArrForm routeEntryArrForm, String username);

	public String execute(String username);

	public String downloadRoute(String username, RouteEntryArrForm routingForm, HttpServletResponse response);

	public RouteUserResponse RouteUserList(String username, String purpose);

	public OptionRouteResponse SearchRoutingBasic(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse SearchRoutingOptional(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse SearchRoutingLookup(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse BasicRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse deleteRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse undoRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse previousRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse hlrRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse optionalRouteBasicRoute(String username, RouteEntryArrForm routingForm);

	public OptionRouteResponse hlrRouteUpdate(String username, HlrEntryArrForm hlrEntryArrForm);

	public OptionRouteResponse hlrRouteUndo(String username, HlrEntryArrForm hlrEntryArrForm);

	public OptionRouteResponse hlrRoutePrevious(String username, HlrEntryArrForm hlrEntryArrForm);

	public OptionRouteResponse hlrRouteBasic(String username, HlrEntryArrForm hlrEntryArrForm);

	public OptionRouteResponse hlrRouteOptional(String username, HlrEntryArrForm hlrEntryArrForm);

//=========================================================================================================

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

	public Map<Integer, Double> getSmscPricing(String smsc, Set<String> networkIds);

}
