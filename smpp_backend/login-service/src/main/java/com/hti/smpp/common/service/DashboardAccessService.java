package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hti.smpp.common.response.DashboardAccessResponse;

@Service
public interface DashboardAccessService {
	public DashboardAccessResponse getDashboardAccess(String username);

	public DashboardAccessResponse updateDashboardVisibility(String username,List<String> dashboardVisibility);
}
