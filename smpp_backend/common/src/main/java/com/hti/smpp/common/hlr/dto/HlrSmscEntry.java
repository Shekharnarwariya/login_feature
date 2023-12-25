package com.hti.smpp.common.hlr.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity bean with JPA annotations
 */

@Entity
@Table(name = "hlr_smsc")
public class HlrSmscEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "name")
	private String name;
	@Column(name = "ip")
	private String ip;
	@Column(name = "port")
	private int port;
	@Column(name = "system_type")
	private String systemType;
	@Column(name = "bindmode")
	private String bindmode;
	@Column(name = "sleep")
	private int sleep;
	@Column(name = "system_id")
	private String systemId;
	@Column(name = "password")
	private String password;
	@Column(name = "bound")
	private boolean bound;
	@Transient
	private String flag;

    /**
     * Default constructor.
     */
	public HlrSmscEntry() {
	}
	 /**
     * Parameterized constructor to initialize HLR/SMSC properties.
     */
	public HlrSmscEntry(String name, String systemId, String password, String ip, int port, String systemType,
			String bindmode, int sleep) {
		this.name = name;
		this.systemId = systemId;
		this.password = password;
		this.ip = ip;
		this.port = port;
		this.systemType = systemType;
		this.bindmode = bindmode;
		this.sleep = sleep;
	}
	 // Getter and setter methods for each property
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSystemType() {
		return systemType;
	}

	public void setSystemType(String systemType) {
		this.systemType = systemType;
	}

	public String getBindmode() {
		return bindmode;
	}

	public void setBindmode(String bindmode) {
		this.bindmode = bindmode;
	}

	public int getSleep() {
		return sleep;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isBound() {
		return bound;
	}

	public void setBound(boolean bound) {
		this.bound = bound;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	@Override
	public String toString() {
		return "HlrSmscEntry: id=" + id + ", name=" + name + ", ip=" + ip + ",port=" + port + ",systemId=" + systemId;
	}
}
