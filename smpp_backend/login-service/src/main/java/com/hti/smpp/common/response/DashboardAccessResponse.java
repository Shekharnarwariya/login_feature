package com.hti.smpp.common.response;

import java.util.Map;

public class DashboardAccessResponse {

	private int userId;
	Map<String,Boolean> visibilityMap;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId2) {
		this.userId = userId2;
	}
	public Map<String, Boolean> getVisibilityMap() {
		return visibilityMap;
	}
	public void setVisibilityMap(Map<String, Boolean> visibilityMap) {
		this.visibilityMap = visibilityMap;
	}
	
}
