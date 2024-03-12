package com.hti.dao;

import java.util.Map;

import com.hti.objects.RoutingDTO;

public interface RouteDAService {
	public Map<Integer, RoutingDTO> listRouting(String systemId);

	public Map<Integer, RoutingDTO> listBasicRouting(String systemId);
	
	public Map<Integer, RoutingDTO> listMmsRouting(String systemId);
}
