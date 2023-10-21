
package com.hti.smpp.common.sms.session;

import org.springframework.stereotype.Component;

import com.logica.smpp.Session;

@Component
public class UserSession {
	private String username;
	private String password;
	private Session session;
	private int commandStatus;
	private String sessionId;
	private boolean busy;

	public UserSession() {

	}

	public UserSession(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean busy) {
		this.busy = busy;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Session getSession() {
		return session;
	}
}
