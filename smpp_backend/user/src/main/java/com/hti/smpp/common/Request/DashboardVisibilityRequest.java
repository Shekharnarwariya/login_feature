package com.hti.smpp.common.Request;

import java.util.List;

public class DashboardVisibilityRequest {

	    private int userId;
	    List<String> dashboardVisibility;

    // Constructor, getters, and setters
    public DashboardVisibilityRequest() {}

    public DashboardVisibilityRequest(int userId,  List<String> dashboardVisibility) {
        this.userId = userId;
        this.dashboardVisibility = dashboardVisibility;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public  List<String> getDashboardVisibility() {
        return dashboardVisibility;
    }

    public void setDashboardVisibility( List<String> dashboardVisibility) {
        this.dashboardVisibility = dashboardVisibility;
    }
}
