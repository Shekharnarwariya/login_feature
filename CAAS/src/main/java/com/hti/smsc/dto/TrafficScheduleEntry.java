package com.hti.smsc.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "smsc_schedule")
public class TrafficScheduleEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "smsc_id", updatable = false)
	private int smscId;
	@Column(name = "gmt")
	private String gmt;
	@Column(name = "day")
	private int day;
	@Column(name = "down_time")
	private String downTime;
	@Column(name = "duration")
	private String duration;
	@Transient
	private String dayName;
	@Transient
	private String smscName;

	public TrafficScheduleEntry(int id, int smscId, String gmt, int day, String duration, String downTime) {
		this.id = id;
		this.smscId = smscId;
		this.gmt = gmt;
		this.day = day;
		this.duration = duration;
		this.downTime = downTime;
	}

	public TrafficScheduleEntry(int smscId, String gmt, int day, String duration, String downTime) {
		this.smscId = smscId;
		this.gmt = gmt;
		this.day = day;
		this.duration = duration;
		this.downTime = downTime;
	}

	public TrafficScheduleEntry() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public String getGmt() {
		return gmt;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getDownTime() {
		return downTime;
	}

	public void setDownTime(String downTime) {
		this.downTime = downTime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getDayName() {
		return dayName;
	}

	public void setDayName(String dayName) {
		this.dayName = dayName;
	}

	public String getSmscName() {
		return smscName;
	}

	public void setSmscName(String smscName) {
		this.smscName = smscName;
	}

	public String toString() {
		return "TrafficScheduleEntry: id=" + id + ",smscId=" + smscId + ",day=" + day + ",gmt=" + gmt + ",Duration="
				+ duration + ",downtime=" + downTime;
	}
}
