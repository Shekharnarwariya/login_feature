package com.hti.smpp.common.responce;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.route.dto.RouteEntryExt;

@Component
public class OptionRouteResponse {

	private List<RouteEntryExt> routinglist;
	private Map<Integer, String> smsclist;
	private Map<Integer, String> groupDetail;

	private String status;

	public List<RouteEntryExt> getRoutinglist() {
		return routinglist;
	}

	public void setRoutinglist(List<RouteEntryExt> routinglist) {
		this.routinglist = routinglist;
	}

	public Map<Integer, String> getSmsclist() {
		return smsclist;
	}

	public void setSmsclist(Map<Integer, String> smsclist) {
		this.smsclist = smsclist;
	}

	public Map<Integer, String> getGroupDetail() {
		return groupDetail;
	}

	public void setGroupDetail(Map<Integer, String> groupDetail) {
		this.groupDetail = groupDetail;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public OptionRouteResponse(List<RouteEntryExt> routinglist, Map<Integer, String> smsclist,
			Map<Integer, String> groupDetail, String status) {
		super();
		this.routinglist = routinglist;
		this.smsclist = smsclist;
		this.groupDetail = groupDetail;
		this.status = status;
	}

	public OptionRouteResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

}
