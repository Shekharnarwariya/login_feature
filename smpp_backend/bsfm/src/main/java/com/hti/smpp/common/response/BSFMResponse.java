package com.hti.smpp.common.response;

import java.util.List;
import java.util.Map;
/**
 * Represents the response object for BSFM-related operations.
 */
public class BSFMResponse {

	private Map<Integer, String> smscList;

	private Map<Integer, String> userlist;

	private Map<Integer, String> groupDetail;

	private Map<String, String> networkmap;

	private Map<Integer, String> operatormap;

	private List<String> users;
//Getter and Setter
	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public Map<Integer, String> getSmscList() {
		return smscList;
	}

	public void setSmscList(Map<Integer, String> smscList) {
		this.smscList = smscList;
	}

	public Map<Integer, String> getUserlist() {
		return userlist;
	}

	public void setUserlist(Map<Integer, String> userlist) {
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
