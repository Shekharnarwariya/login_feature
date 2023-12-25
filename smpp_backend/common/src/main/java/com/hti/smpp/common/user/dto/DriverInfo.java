package com.hti.smpp.common.user.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing user-specific delivery receipt (DLR) settings with JPA annotations.
 */
@Entity
@Table(name = "driver_info")
public class DriverInfo {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "driver")
	private String driver;

	@Column(name = "updateOn")
	private LocalDateTime updateOn;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public LocalDateTime getUpdateOn() {
		return updateOn;
	}

	public void setUpdateOn(LocalDateTime updateOn) {
		this.updateOn = updateOn;
	}

	public DriverInfo(int id, String driver, LocalDateTime updateOn) {
		super();
		this.id = id;
		this.driver = driver;
		this.updateOn = updateOn;
	}

	public DriverInfo(int id, String driver) {
		super();
		this.id = id;
		this.driver = driver;
	}

	@Override
	public String toString() {
		return "DriverInfo [id=" + id + ", driver=" + driver + ", updateOn=" + updateOn + "]";
	}

	public DriverInfo() {
		super();
	}

}