package com.hti.objects;

public class SignalRetryObj {
	private String messageId;
	private long processTime;
	private RoutePDU routePDU;
	private Integer[] criteria;

	public SignalRetryObj(String messageId, long processTime) {
		this.messageId = messageId;
		this.processTime = processTime;
	}

	public SignalRetryObj(RoutePDU routePDU, Integer[] criteria) {
		this.routePDU = routePDU;
		this.criteria = criteria;
	}

	public Integer[] getCriteria() {
		return criteria;
	}

	public RoutePDU getRoutePDU() {
		return routePDU;
	}

	public String getMessageId() {
		return messageId;
	}

	public long getProcessTime() {
		return processTime;
	}
}
