package com.hti.smpp.common.response;

import java.util.Collection;
import java.util.Map;

public class SetupAlertResponse {

	Collection<String> smscList;
	Map<Integer, String> countries;
	public Collection<String> getSmscList() {
		return smscList;
	}
	public void setSmscList(Collection<String> smscList) {
		this.smscList = smscList;
	}
	public Map<Integer, String> getCountries() {
		return countries;
	}
	public void setCountries(Map<Integer, String> countries) {
		this.countries = countries;
	}
	
	
}
