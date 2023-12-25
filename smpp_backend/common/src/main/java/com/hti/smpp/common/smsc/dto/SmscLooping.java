package com.hti.smpp.common.smsc.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
/**
 * Entity class representing custom settings for a specific SMSC with JPA annotations.
 */
@Entity
@Table(name = "smsc_looping_rule")
public class SmscLooping {
	
	@Id
	@Column(name = "smsc_id", unique = true, nullable = false)
	private int smscId;
	@Column(name = "sender_id")
	private String senderId;
	@Column(name = "duration")
	private int duration;
	@Column(name = "count")
	private int count;
	@Column(name = "reroute_smsc_id")
	private int rerouteSmscId;
	@Column(name = "active")
	private boolean active;
	@Transient
	private String smsc;
	@Transient
	private String rerouteSmsc;

	public SmscLooping() {
	}

	public SmscLooping(int smscId, String senderId, int duration, int count, int rerouteSmscId) {
		this.smscId = smscId;
		this.senderId = senderId;
		this.duration = duration;
		this.count = count;
		this.rerouteSmscId = rerouteSmscId;
	}

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getRerouteSmscId() {
		return rerouteSmscId;
	}

	public void setRerouteSmscId(int rerouteSmscId) {
		this.rerouteSmscId = rerouteSmscId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getRerouteSmsc() {
		return rerouteSmsc;
	}

	public void setRerouteSmsc(String rerouteSmsc) {
		this.rerouteSmsc = rerouteSmsc;
	}

	public String toString() {
		return "SmscLooping: smscId=" + smscId + ",senderId=" + senderId + ",loopCount=" + count + ",duration="
				+ duration + ",rerouteSmscId=" + rerouteSmscId + ",active=" + active;
	}
}
