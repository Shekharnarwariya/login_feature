package com.hti.smpp.common.response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.user.dto.UserEntry;

public class RouteUserResponse {

	Map<String, String> networkmap;

	Map<Integer, String> operatormap;

	Map<Integer, String> smsclist;

	Map<Integer, String> listGroupNames;

	TreeSet<String> treeSet;

	Set<String> smscTypes;

	List<UserEntryExt> usernames;

	List<UserEntry> filter;

	String status;

	Map<Integer, UserEntryExt> userEntries;

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

	public Map<Integer, String> getSmsclist() {
		return smsclist;
	}

	public void setSmsclist(Map<Integer, String> smsclist) {
		this.smsclist = smsclist;
	}

	public Map<Integer, String> getListGroupNames() {
		return listGroupNames;
	}

	public void setListGroupNames(Map<Integer, String> listGroupNames) {
		this.listGroupNames = listGroupNames;
	}

	public TreeSet<String> getTreeSet() {
		return treeSet;
	}

	public void setTreeSet(TreeSet<String> treeSet) {
		this.treeSet = treeSet;
	}

	public Set<String> getSmscTypes() {
		return smscTypes;
	}

	public void setSmscTypes(Set<String> smscTypes) {
		this.smscTypes = smscTypes;
	}

	public List<UserEntryExt> getUsernames() {
		return usernames;
	}

	public void setUsernames(List<UserEntryExt> usernames) {
		this.usernames = usernames;
	}

	public List<UserEntry> getFilter() {
		return filter;
	}

	public void setFilter(List<UserEntry> filter) {
		this.filter = filter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Map<Integer, UserEntryExt> getUserEntries() {
		return userEntries;
	}

	public void setUserEntries(Map<Integer, UserEntryExt> userEntries) {
		this.userEntries = userEntries;
	}

}
