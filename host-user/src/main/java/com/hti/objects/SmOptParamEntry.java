package com.hti.objects;

import com.logica.smpp.pdu.tlv.TLV;

public class SmOptParamEntry {
	private TLV peId;
	private TLV templateId;
	private TLV telemarketerId;
	private TLV channelType;
	private TLV caption;
	private String messageId;

	public SmOptParamEntry(TLV peId, TLV templateId, TLV telemarketerId, String messageId) {
		this.peId = peId;
		this.templateId = templateId;
		this.telemarketerId = telemarketerId;
		this.messageId = messageId;
	}

	public SmOptParamEntry(TLV channelType, TLV caption, String messageId) {
		super();
		this.channelType = channelType;
		this.caption = caption;
		this.messageId = messageId;
	}

	public TLV getPeId() {
		return peId;
	}

	public void setPeId(TLV peId) {
		this.peId = peId;
	}

	public TLV getTemplateId() {
		return templateId;
	}

	public void setTemplateId(TLV templateId) {
		this.templateId = templateId;
	}

	public TLV getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(TLV telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	public TLV getChannelType() {
		return channelType;
	}

	public void setChannelType(TLV channelType) {
		this.channelType = channelType;
	}

	public TLV getCaption() {
		return caption;
	}

	public void setCaption(TLV caption) {
		this.caption = caption;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
}
