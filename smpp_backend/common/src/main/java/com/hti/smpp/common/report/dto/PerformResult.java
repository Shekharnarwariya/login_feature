package com.hti.smpp.common.report.dto;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "perform_result")
public class PerformResult {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "alert_id")
	private int alertId;
	@Column(name = "start_time")
	private String startTime;
	@Column(name = "end_time")
	private String endTime;
	@Column(name = "percent")
	private int resultedPercent;
	@Column(name = "route")
	private String route;
	@Column(name = "network")
	private int networkId;
	@Column(name = "count")
	private int count;
	@Column(name = "statusCount")
	private int statusCount;
	@Column(name = "sender")
	private String sender;
	@Transient
	private String country;
	@Transient
	private String operator;
	@Transient
	private String status;
	@Transient
	private int expectedPercent;

	public int getAlertId() {
		return alertId;
	}

	public void setAlertId(int alertId) {
		this.alertId = alertId;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getResultedPercent() {
		return resultedPercent;
	}

	public void setResultedPercent(int resultedPercent) {
		this.resultedPercent = resultedPercent;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(int statusCount) {
		this.statusCount = statusCount;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getExpectedPercent() {
		return expectedPercent;
	}

	public void setExpectedPercent(int expectedPercent) {
		this.expectedPercent = expectedPercent;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String toString() {
		return "performResult: id=" + id + ",alertId=" + alertId + ",resultedPercent=" + resultedPercent + ",route="
				+ route + ",network=" + networkId + ",count=" + count + ",statusCount=" + statusCount;
	}
}
