package com.hti.dao;

import java.util.Map;

import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.MmsRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.route.dto.RouteEntry;

public interface RouteDAService {
	public Map<Integer, RouteEntry> listBasic();

	public Map<Integer, RouteEntry> listBasic(int user_id);
	
	public Map<Integer,Map<Integer, RouteEntry>> listBasic(Integer[] user_id);

	public Map<Integer, OptionalRouteEntry> listOptional();

	public Map<Integer, OptionalRouteEntry> listOptional(Integer[] route_id);

	public Map<Integer, HlrRouteEntry> listHlr();

	public Map<Integer, HlrRouteEntry> listHlr(Integer[] route_id);
	
	public Map<Integer, MmsRouteEntry> listMms();

	public Map<Integer, MmsRouteEntry> listMms(Integer[] route_id);
}
