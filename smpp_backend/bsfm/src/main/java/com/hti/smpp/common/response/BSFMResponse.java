package com.hti.smpp.common.response;

import java.util.Collection;
import java.util.Map;
/**
 * Represents the response object for BSFM-related operations.
 */
public class BSFMResponse {

	private Collection<String> smscList;

	private Collection<String> userlist;

	private Map<Integer, String> groupDetail;

	private Map<String, String> networkmap;

	private Map<Integer, String> operatormap;

	public Collection<String> getSmscList() {
		return smscList;
	}

	public void setSmscList(Collection<String> smscList) {
		this.smscList = smscList;
	}

	public Collection<String> getUserlist() {
		return userlist;
	}

	public void setUserlist(Collection<String> userlist) {
		this.userlist = userlist;
	}

	public Map<Integer, String> getGroupDetail() {
		return groupDetail;
	}

	public void setGroupDetail(Map<Integer, String> groupDetail) {
		this.groupDetail = groupDetail;
	}

	public Map<String, String> getNetworkmap() {
		return networkmap;
	}

	public void setNetworkmap(Map<String, String> networkmap) {
		this.networkmap = networkmap;
	}

	public Map<Integer, String> getOperatormap() {
		return operatormap;
	}

	public void setOperatormap(Map<Integer, String> operatormap) {
		this.operatormap = operatormap;
	}

}
