package com.hti.smpp.common.dto;

import org.springframework.stereotype.Component;

@Component
public class Json {
	private Long destination;

	private String content;

	private String flag = "F";

	public Long getDestination() {
		return destination;
	}

	public void setDestination(Long destination) {
		this.destination = destination;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	
}
