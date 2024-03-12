/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.objects;

/**
 *
 * @author Administrator
 */
public class SmscDTO {
	private int id;
	private String smscid;
	private String ip;
	private String backupIp;
	private int port;
	private String bindmode;
	private String systemid;
	private String password;
	private String systemType;
	private int sleepTime;
	private int sessionId;
	private int ston;
	private int snpi;
	private int dton;
	private int dnpi;
	private boolean enforcetonnpi;
	private boolean SameDestSource;
	private boolean alert;
	private String alertNumber;
	private String alertEmail;
	private String destPrefix;

	public SmscDTO() {
	}

	public SmscDTO(int id, String ip, String backupIp, int port, String bindmode, String systemId, String password,
			String systemType, String smscid, int sleepTime, int sessionId, int ston, int snpi, int dton, int dnpi,
			boolean enforcetonnpi, boolean SameDestSource, String destPrefix) {
		this.id = id;
		this.ip = ip;
		this.backupIp = backupIp;
		this.port = port;
		this.bindmode = bindmode;
		this.systemid = systemId;
		this.password = password;
		this.systemType = systemType;
		this.smscid = smscid;
		this.sleepTime = sleepTime;
		this.sessionId = sessionId;
		this.ston = ston;
		this.snpi = snpi;
		this.dton = dton;
		this.dnpi = dnpi;
		this.enforcetonnpi = enforcetonnpi;
		this.SameDestSource = SameDestSource;
		this.destPrefix = destPrefix;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDestPrefix() {
		return destPrefix;
	}

	public void setDestPrefix(String destPrefix) {
		this.destPrefix = destPrefix;
	}

	public boolean isEnforcetonnpi() {
		return enforcetonnpi;
	}

	public boolean isSameDestSource() {
		return SameDestSource;
	}

	public String getBackupIp() {
		return backupIp;
	}

	public String getPassword() {
		return password;
	}

	public String getSystemType() {
		return systemType;
	}

	public int getSleepTime() {
		return sleepTime;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getSton() {
		return ston;
	}

	public int getSnpi() {
		return snpi;
	}

	public int getDton() {
		return dton;
	}

	public int getDnpi() {
		return dnpi;
	}

	public String getSmscid() {
		return smscid;
	}

	public void setSmscid(String smscid) {
		this.smscid = smscid;
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

	public String getBindmode() {
		return bindmode;
	}

	public void setBindmode(String bindmode) {
		this.bindmode = bindmode;
	}

	public String getSystemid() {
		return systemid;
	}

	public void setSystemid(String systemid) {
		this.systemid = systemid;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	public String getAlertNumber() {
		return alertNumber;
	}

	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}

	public String getAlertEmail() {
		return alertEmail;
	}

	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}
}
