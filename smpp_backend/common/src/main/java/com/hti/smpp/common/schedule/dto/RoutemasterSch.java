package com.hti.smpp.common.schedule.dto;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "routemaster_sch")
public class RoutemasterSch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "user_id")
	private int userId;

	@Column(name = "network_id")
	private int networkId;

	@Column(name = "smsc_id")
	private int smscId;

	@Column(name = "group_id")
	private int groupId;

	private Double cost;

	@Column(name = "smsc_type")
	private String smscType;

	@Column(name = "schedule_by")
	private String scheduleBy;

	@Column(name = "schedule_on")
	private String scheduleOn;

	private String remarks;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public Double getCost() {
		return cost;
	}

	public void setCost(Double cost) {
		this.cost = cost;
	}

	public String getSmscType() {
		return smscType;
	}

	public void setSmscType(String smscType) {
		this.smscType = smscType;
	}

	public String getScheduleBy() {
		return scheduleBy;
	}

	public void setScheduleBy(String scheduleBy) {
		this.scheduleBy = scheduleBy;
	}

	public String getScheduleOn() {
		return scheduleOn;
	}

	public void setScheduleOn(String scheduleOn) {
		this.scheduleOn = scheduleOn;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
