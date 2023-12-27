package com.hti.smpp.common.twoway.request;
/**
 *  The `KeywordEntryForm` class represents the form for creating or updating a keyword entry.
 */
public class KeywordEntryForm {
	
	private int id;
	private int userId;
	private String prefix;
	private String suffix;
	private String type;
	private String shortCode;
	private String expiresOn;
	private boolean reply;
	private String replyMessage;
	private String replyOnFailed;
	private String replyOnExpire;
	private String alertNumber;
	private String alertEmail;
	private String alertUrl;
	private String replySender;
	private String sources;
	public KeywordEntryForm() {
	
	}
	/**
	 * Parameterized constructor for creating a `KeywordEntryForm` instance with provided values.
	 * @param id
	 * @param userId
	 * @param prefix
	 * @param suffix
	 * @param type
	 * @param shortCode
	 * @param expiresOn
	 * @param reply
	 * @param replyMessage
	 * @param replyOnFailed
	 * @param replyOnExpire
	 * @param alertNumber
	 * @param alertEmail
	 * @param alertUrl
	 * @param replySender
	 * @param sources
	 */
	public KeywordEntryForm(int id, int userId, String prefix, String suffix, String type, String shortCode,
			String expiresOn, boolean reply, String replyMessage, String replyOnFailed, String replyOnExpire,
			String alertNumber, String alertEmail, String alertUrl, String replySender, String sources) {
		super();
		this.id = id;
		this.userId = userId;
		this.prefix = prefix;
		this.suffix = suffix;
		this.type = type;
		this.shortCode = shortCode;
		this.expiresOn = expiresOn;
		this.reply = reply;
		this.replyMessage = replyMessage;
		this.replyOnFailed = replyOnFailed;
		this.replyOnExpire = replyOnExpire;
		this.alertNumber = alertNumber;
		this.alertEmail = alertEmail;
		this.alertUrl = alertUrl;
		this.replySender = replySender;
		this.sources = sources;
	}
	//Getter and Setter 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getShortCode() {
		return shortCode;
	}
	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}
	public String getExpiresOn() {
		return expiresOn;
	}
	public void setExpiresOn(String expiresOn) {
		this.expiresOn = expiresOn;
	}
	public boolean isReply() {
		return reply;
	}
	public void setReply(boolean reply) {
		this.reply = reply;
	}
	public String getReplyMessage() {
		return replyMessage;
	}
	public void setReplyMessage(String replyMessage) {
		this.replyMessage = replyMessage;
	}
	public String getReplyOnFailed() {
		return replyOnFailed;
	}
	public void setReplyOnFailed(String replyOnFailed) {
		this.replyOnFailed = replyOnFailed;
	}
	public String getReplyOnExpire() {
		return replyOnExpire;
	}
	public void setReplyOnExpire(String replyOnExpire) {
		this.replyOnExpire = replyOnExpire;
	}
	public String getAlertNumber() {
		return alertNumber;
	}
	public void setAlertNumber(String alertNumber) {
		this.alertNumber = alertNumber;
	}
	public String getAlertEmail() {
		return alertEmail;
	}
	public void setAlertEmail(String alertEmail) {
		this.alertEmail = alertEmail;
	}
	public String getAlertUrl() {
		return alertUrl;
	}
	public void setAlertUrl(String alertUrl) {
		this.alertUrl = alertUrl;
	}
	public String getReplySender() {
		return replySender;
	}
	public void setReplySender(String replySender) {
		this.replySender = replySender;
	}
	public String getSources() {
		return sources;
	}
	public void setSources(String sources) {
		this.sources = sources;
	}
	
}
