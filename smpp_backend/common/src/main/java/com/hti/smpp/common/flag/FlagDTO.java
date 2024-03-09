package com.hti.smpp.common.flag;

import java.io.Serializable;

public class FlagDTO implements Serializable {

	private String path;

	private String requestType;

	private String content;

	private String id;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "FlagDTO [path=" + path + ", requestType=" + requestType + ", content=" + content + ", id=" + id + "]";
	}

}
