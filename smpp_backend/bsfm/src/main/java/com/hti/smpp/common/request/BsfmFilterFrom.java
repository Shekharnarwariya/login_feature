package com.hti.smpp.common.request;
/**
 * Represents the filter form for Bsfm profiles.
 */
public class BsfmFilterFrom {
	private int id;
	private String profilename;
	private String[] username;
	private String content;
	private String[] smsc;
	private String prefixes;
	private String sourceid;
	private boolean active;
	private String flag;
	private boolean reverse;
	private String reroute;
	private boolean schedule;
	private String dayTime;
	private boolean activeOnScheduleTime;
	private String senderType;
	private String masterId;
	private String forceSenderId;
	private int rerouteGroupId;
	private int msgLength;
	private int lengthOpr;
	private String[] networks;
	private String[] mcc;
	private boolean countryWise;

	//Getter and setter methods for BsfmFilterFrom properties.
	public boolean isActiveOnScheduleTime() {
		return activeOnScheduleTime;
	}

	public void setActiveOnScheduleTime(boolean activeOnScheduleTime) {
		this.activeOnScheduleTime = activeOnScheduleTime;
	}

	public String getForceSenderId() {
		return forceSenderId;
	}

	public void setForceSenderId(String forceSenderId) {
		this.forceSenderId = forceSenderId;
	}

	public int getRerouteGroupId() {
		return rerouteGroupId;
	}

	public void setRerouteGroupId(int rerouteGroupId) {
		this.rerouteGroupId = rerouteGroupId;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}

	public String getReroute() {
		return reroute;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
	}

	/**
	 * @return the profilename
	 */
	public String getProfilename() {
		return profilename;
	}

	/**
	 * @param profilename the profilename to set
	 */
	public void setProfilename(String profilename) {
		this.profilename = profilename;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the prefixes
	 */
	public String getPrefixes() {
		return prefixes;
	}

	/**
	 * @param prefixes the prefixes to set
	 */
	public void setPrefixes(String prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * @return the sourceid
	 */
	public String getSourceid() {
		return sourceid;
	}

	/**
	 * @param sourceid the sourceid to set
	 */
	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public String getDayTime() {
		return dayTime;
	}

	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}

	public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public String[] getUsername() {
		return username;
	}

	public void setUsername(String[] username) {
		this.username = username;
	}

	public String[] getSmsc() {
		return smsc;
	}

	public void setSmsc(String[] smsc) {
		this.smsc = smsc;
	}

	public int getMsgLength() {
		return msgLength;
	}

	public void setMsgLength(int msgLength) {
		this.msgLength = msgLength;
	}

	public int getLengthOpr() {
		return lengthOpr;
	}

	public void setLengthOpr(int lengthOpr) {
		this.lengthOpr = lengthOpr;
	}

	public String[] getNetworks() {
		return networks;
	}

	public void setNetworks(String[] networks) {
		this.networks = networks;
	}

	public String[] getMcc() {
		return mcc;
	}

	public void setMcc(String[] mcc) {
		this.mcc = mcc;
	}

	public boolean isCountryWise() {
		return countryWise;
	}

	public void setCountryWise(boolean countryWise) {
		this.countryWise = countryWise;
	}

}
