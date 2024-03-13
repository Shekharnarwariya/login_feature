package com.hti.smpp.common.httpclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseApiDTO implements java.io.Serializable {
	private int id;
	private String username;
	private String password;
	private String sender;
	private int type;
	private int format;
	private String text;
	private String scheduleTime;
	private String gmt;
	private List<String> receipients;
	private List<String[]> customReceipients;
	private String webid;
	private String ipAddr;
	private String requestFormat;
	private String accessKey;
	private int limit;
	private Set<String> group;
	private String scheduleFile;
	private String serverScheduleTime;
	private int scheduleId;
	// ------ optional parameters for submit_sm ------
	private String peId;
	private String templateId;
	private String telemarketerId;
	private String caption;
	private boolean mms;
	private String mmsType;

	public BaseApiDTO() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getScheduleTime() {
		return scheduleTime;
	}

	public void setScheduleTime(String scheduleTime) {
		this.scheduleTime = scheduleTime;
	}

	public String getGmt() {
		return gmt;
	}

	public void setGmt(String gmt) {
		this.gmt = gmt;
	}

	public List<String> getReceipients() {
		if (receipients == null) {
			receipients = new ArrayList<String>();
		}
		return receipients;
	}

	public void setReceipients(List<String> receipients) {
		this.receipients = receipients;
	}

	public String getWebid() {
		return webid;
	}

	public void setWebid(String webid) {
		this.webid = webid;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public List<String[]> getCustomReceipients() {
		if (customReceipients == null) {
			customReceipients = new ArrayList<String[]>();
		}
		return customReceipients;
	}

	public void setCustomReceipients(List<String[]> customReceipients) {
		this.customReceipients = customReceipients;
	}

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public String getRequestFormat() {
		return requestFormat;
	}

	public void setRequestFormat(String requestFormat) {
		this.requestFormat = requestFormat;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getPeId() {
		return peId;
	}

	public void setPeId(String peId) {
		this.peId = peId;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getTelemarketerId() {
		return telemarketerId;
	}

	public void setTelemarketerId(String telemarketerId) {
		this.telemarketerId = telemarketerId;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public Set<String> getGroup() {
		if (group == null) {
			group = new HashSet<String>();
		}
		return group;
	}

	public void setGroup(Set<String> group) {
		this.group = group;
	}

	public String getScheduleFile() {
		return scheduleFile;
	}

	public void setScheduleFile(String scheduleFile) {
		this.scheduleFile = scheduleFile;
	}

	public String getServerScheduleTime() {
		return serverScheduleTime;
	}

	public void setServerScheduleTime(String serverScheduleTime) {
		this.serverScheduleTime = serverScheduleTime;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public boolean isMms() {
		return mms;
	}

	public void setMms(boolean mms) {
		this.mms = mms;
	}

	public String getMmsType() {
		return mmsType;
	}

	public void setMmsType(String mmsType) {
		this.mmsType = mmsType;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String toString() {
		return requestFormat + ": id=" + id + ",Username=" + username + ",Password=" + password + ",format=" + format
				+ ",sender=" + sender + ",Type=" + type + ",Text=" + text + ",scheduleTime=" + scheduleTime + ",Gmt="
				+ gmt + ",Numbers=" + receipients;
	}
}
