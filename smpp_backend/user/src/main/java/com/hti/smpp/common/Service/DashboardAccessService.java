package com.hti.smpp.common.Service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.DashboardAccess.Entity.DashboardAccess;

@Service
public interface DashboardAccessService {
	public DashboardAccess saveDashboardAccess(String username);

	public void updateDashboardVisibility(String username,int userId,List<String> dashboardVisibility);
}
