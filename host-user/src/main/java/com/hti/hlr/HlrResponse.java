package com.hti.hlr;

public class HlrResponse {
	private String responseId;
	private int commandId;
	private int commandStatus;
	private String status;
	private String error;
	private String nnc;
	private boolean ported;
	private boolean roaming;
	private String portedNNC;
	private String roamingNNC;
	private int permanent;
	private boolean dnd;

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public int getCommandId() {
		return commandId;
	}

	public void setCommandId(int commandId) {
		this.commandId = commandId;
	}

	public int getCommandStatus() {
		return commandStatus;
	}

	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getNnc() {
		return nnc;
	}

	public void setNnc(String nnc) {
		this.nnc = nnc;
	}

	public boolean isPorted() {
		return ported;
	}

	public void setPorted(boolean ported) {
		this.ported = ported;
	}

	public boolean isRoaming() {
		return roaming;
	}

	public void setRoaming(boolean roaming) {
		this.roaming = roaming;
	}

	public String getPortedNNC() {
		return portedNNC;
	}

	public void setPortedNNC(String portedNNC) {
		this.portedNNC = portedNNC;
	}

	public String getRoamingNNC() {
		return roamingNNC;
	}

	public void setRoamingNNC(String roamingNNC) {
		this.roamingNNC = roamingNNC;
	}

	public int getPermanent() {
		return permanent;
	}

	public void setPermanent(int permanent) {
		this.permanent = permanent;
	}

	public boolean isDnd() {
		return dnd;
	}

	public void setDnd(boolean dnd) {
		this.dnd = dnd;
	}

	public String toString() {
		return "hlrResponse: respId=" + responseId + ",commandId=" + commandId + ",status=" + status;
	}
}
