package com.hti.smpp.common.response;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hti.smpp.common.util.dto.AlertDTO;

public class AlertEditResponse {

	private Map<Integer, String> networkmap;
	private List<String> smscList;
	private Map<Integer, String> existCountries;
	private Set<String> existRoutes;
	private AlertDTO alert;
	
	
	
	@Override
	public String toString() {
		return "AlertEditResponse [networkmap=" + networkmap + ", smscList=" + smscList + ", existCountries="
				+ existCountries + ", existRoutes=" + existRoutes + ", alert=" + alert + "]";
	}
	public Map<Integer, String> getNetworkmap() {
		return networkmap;
	}
	public void setNetworkmap(Map<Integer, String> networkmap) {
		this.networkmap = networkmap;
	}
	public List<String> getSmscList() {
		return smscList;
	}
	public void setSmscList(List<String> smscList) {
		this.smscList = smscList;
	}
	public Map<Integer, String> getExistCountries() {
		return existCountries;
	}
	public void setExistCountries(Map<Integer, String> existCountries) {
		this.existCountries = existCountries;
	}
	public Set<String> getExistRoutes() {
		return existRoutes;
	}
	public void setExistRoutes(Set<String> existRoutes) {
		this.existRoutes = existRoutes;
	}
	public AlertDTO getAlert() {
		return alert;
	}
	public void setAlert(AlertDTO alert) {
		this.alert = alert;
	}
	
	
	
}
