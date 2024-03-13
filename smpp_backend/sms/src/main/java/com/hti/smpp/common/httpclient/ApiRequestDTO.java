package com.hti.smpp.common.httpclient;

import java.util.ArrayList;
import java.util.List;

public class ApiRequestDTO extends BaseApiDTO {
	private int sourceTon;
	private int sourceNpi;
	private String messageType;
	private List<String> responselist;

	public ApiRequestDTO() {
	}

	public ApiRequestDTO(String webId, List<String> responselist, String username) {
		setWebid(webId);
		this.responselist = responselist;
		setUsername(username);
	}

	public ApiRequestDTO(String username, String password, String sender, int sourceTon, int sourceNpi, String text,
			String messageType, List<String> receipients, String requestFormat, String peId, String templateId,
			String telemarketerId) {
		setUsername(username);
		setPassword(password);
		setSender(sender);
		setText(text);
		setReceipients(receipients);
		setRequestFormat(requestFormat);
		this.sourceNpi = sourceNpi;
		this.sourceTon = sourceTon;
		this.messageType = messageType;
		setPeId(peId);
		setTemplateId(templateId);
		setTelemarketerId(telemarketerId);
	}

	public ApiRequestDTO(String username, String password, String sender, int sourceTon, int sourceNpi, String text,
			String messageType, List<String> receipients, String requestFormat, String peId, String templateId,
			String telemarketerId, String mmsType, String caption) {
		setUsername(username);
		setPassword(password);
		setSender(sender);
		setText(text);
		setReceipients(receipients);
		setRequestFormat(requestFormat);
		this.sourceNpi = sourceNpi;
		this.sourceTon = sourceTon;
		this.messageType = messageType;
		setPeId(peId);
		setTemplateId(templateId);
		setTelemarketerId(telemarketerId);
		setMmsType(mmsType);
		setCaption(caption);
	}

	public ApiRequestDTO(String username, String password, String sender, int sourceTon, int sourceNpi,
			String messageType, List<String[]> customReceipients, String requestFormat, String peId, String templateId,
			String telemarketerId) {
		setUsername(username);
		setPassword(password);
		setSender(sender);
		setCustomReceipients(customReceipients);
		setRequestFormat(requestFormat);
		this.sourceNpi = sourceNpi;
		this.sourceTon = sourceTon;
		this.messageType = messageType;
		setPeId(peId);
		setTemplateId(templateId);
		setTelemarketerId(telemarketerId);
	}

	public int getSourceTon() {
		return sourceTon;
	}

	public void setSourceTon(int sourceTon) {
		this.sourceTon = sourceTon;
	}

	public int getSourceNpi() {
		return sourceNpi;
	}

	public void setSourceNpi(int sourceNpi) {
		this.sourceNpi = sourceNpi;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public List<String> getResponselist() {
		if (responselist == null) {
			responselist = new ArrayList<String>();
		}
		return responselist;
	}

	public void setResponselist(List<String> responselist) {
		this.responselist = responselist;
	}
}
