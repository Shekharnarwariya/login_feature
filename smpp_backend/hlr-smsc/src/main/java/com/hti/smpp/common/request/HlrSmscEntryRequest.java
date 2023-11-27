package com.hti.smpp.common.request;

public class HlrSmscEntryRequest {

	private String name;

	private String ip;

	private int port;

	private String bindmode;

	private int sleep;

	private String password;

	private boolean bound;

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
}
