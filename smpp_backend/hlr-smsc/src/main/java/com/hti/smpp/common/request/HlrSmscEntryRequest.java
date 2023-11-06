package com.hti.smpp.common.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class HlrSmscEntryRequest {

	@NotEmpty
	private String name;

	@NotEmpty
	private String ip;

	@Positive
	private int port;

	@Pattern(regexp = "YOUR_REGEX_PATTERN_HERE")
	private String bindmode;

	@Positive
	private int sleep;

	@NotEmpty
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
