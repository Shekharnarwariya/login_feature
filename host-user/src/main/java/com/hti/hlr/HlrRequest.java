package com.hti.hlr;

public class HlrRequest {
	private String messageId;
	private String destination;
	private HlrResponse response;

	public HlrRequest() {
	}

	public HlrRequest(String messageId, String destination) {
		this.messageId = messageId;
		this.destination = destination;
	}

	public HlrRequest(String messageId, int commandId, int commandStatus, String responseId) {
		this.messageId = messageId;
		getResponse().setCommandId(commandId);
		getResponse().setCommandStatus(commandStatus);
		getResponse().setResponseId(responseId);
	}

	public HlrRequest(String messageId, HlrResponse response) {
		this.messageId = messageId;
		this.response = response;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public HlrResponse getResponse() {
		if (response == null) {
			response = new HlrResponse();
		}
		return response;
	}

	public void setResponse(HlrResponse response) {
		this.response = response;
	}

	public String toString() {
		return "hlrRequest: msgid=" + messageId + ",destination=" + destination + ", " + response;
	}
}
