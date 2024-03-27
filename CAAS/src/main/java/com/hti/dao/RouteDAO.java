package com.hti.dao;

import java.util.List;

import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.MmsRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;

public interface RouteDAO {
	/*
	 * public List<ProfileAllocEntry> listProfileAllocation();
	 * 
	 * public List<ProfileAllocEntry> listProfileAllocation(int userId);
	 */
	public List<RouteEntry> listBasic();

	public List<RouteEntry> listBasic(int user_id);
	
	public List<RouteEntry> listBasic(Integer[] user_id);

	public List<OptionalRouteEntry> listOptional();

	public List<OptionalRouteEntry> listOptional(Integer[] route_id);

	public List<HlrRouteEntry> listHlr();

	public List<HlrRouteEntry> listHlr(Integer[] route_id);
	
	public List<MmsRouteEntry> listMms();

	public List<MmsRouteEntry> listMms(Integer[] route_id);
}
