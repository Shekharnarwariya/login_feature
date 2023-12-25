package com.hti.smpp.common.response;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hti.smpp.common.bsfm.dto.Bsfm;
/**
 * Response object for profile deletion.
 * Contains various lists and maps related to the deletion operation.
 */
public class DeleteProfileResponse {

	private List<String[]> daytimelist;
	private Bsfm bsfm;
	private List<String> underUserList;
	private Collection<String> smscList;
	private List<String> existUserList;
	private List<String> existSmscList;
	private Map<Integer, String> grouping;
	private Map<Integer, String> networkmap;
	private Map<Integer, String> existNetworks;
	
//Getter and setter methods for the properties...
	
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String[]> getDaytimelist() {
		return daytimelist;
	}

	public void setDaytimelist(List<String[]> daytimelist) {
		this.daytimelist = daytimelist;
	}

	public Bsfm getBsfm() {
		return bsfm;
	}

	public void setBsfm(Bsfm bsfm) {
		this.bsfm = bsfm;
	}

	public List<String> getUnderUserList() {
		return underUserList;
	}

	public void setUnderUserList(List<String> underUserList) {
		this.underUserList = underUserList;
	}

	public Collection<String> getSmscList() {
		return smscList;
	}

	public void setSmscList(Collection<String> smscList) {
		this.smscList = smscList;
	}

	public List<String> getExistUserList() {
		return existUserList;
	}

	public void setExistUserList(List<String> existUserList) {
		this.existUserList = existUserList;
	}

	public List<String> getExistSmscList() {
		return existSmscList;
	}

	public void setExistSmscList(List<String> existSmscList) {
		this.existSmscList = existSmscList;
	}

	public Map<Integer, String> getGrouping() {
		return grouping;
	}

	public void setGrouping(Map<Integer, String> grouping) {
		this.grouping = grouping;
	}

	public Map<Integer, String> getNetworkmap() {
		return networkmap;
	}

	public void setNetworkmap(Map<Integer, String> networkmap) {
		this.networkmap = networkmap;
	}

	public Map<Integer, String> getExistNetworks() {
		return existNetworks;
	}

	public void setExistNetworks(Map<Integer, String> existNetworks) {
		this.existNetworks = existNetworks;
	}

}
