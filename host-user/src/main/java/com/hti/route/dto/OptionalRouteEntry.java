package com.hti.route.dto;

import java.io.Serializable;
import java.util.Map;

public class OptionalRouteEntry implements Serializable {
	private int routeId;
	private int numSmscId;
	private int backupSmscId;
	private String forceSenderNum;
	private String forceSenderAlpha;
	private String expiredOn = "0";
	private int smsLength;
	private boolean refund;
	private String regSender;
	private int regSmscId;
	private int regGroupId;
	private int codeLength;
	private boolean replaceContent;
	private String replacement;
	private String msgAppender;
	private String sourceAppender;
	private String editBy;
	private String editOn;
	private String senderReplFrom;
	private String senderReplTo;
	private Map<String, String> replaceContentMap;

	public OptionalRouteEntry(String editBy, String editOn) {
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public OptionalRouteEntry() {
	}

	/*
	 * public OptionalRouteEntry(int routeId) { this.routeId = routeId; }
	 */
	public OptionalRouteEntry(int routeId, int numSmscId, int backupSmscId, String forceSenderNum,
			String forceSenderAlpha, String expiredOn, int smsLength, boolean refund, String regSender, int regSmscId,
			int codeLength, boolean replaceContent, String replacement, String msgAppender, String sourceAppender,
			String editBy, String editOn) {
		this.routeId = routeId;
		this.numSmscId = numSmscId;
		this.backupSmscId = backupSmscId;
		this.forceSenderNum = forceSenderNum;
		this.forceSenderAlpha = forceSenderAlpha;
		this.expiredOn = expiredOn;
		this.smsLength = smsLength;
		this.refund = refund;
		this.regSender = regSender;
		this.regSmscId = regSmscId;
		this.codeLength = codeLength;
		this.replaceContent = replaceContent;
		this.replacement = replacement;
		this.msgAppender = msgAppender;
		this.sourceAppender = sourceAppender;
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public String getEditOn() {
		return editOn;
	}

	public void setEditOn(String editOn) {
		this.editOn = editOn;
	}

	public String getEditBy() {
		return editBy;
	}

	public void setEditBy(String editBy) {
		this.editBy = editBy;
	}

	public int getRouteId() {
		return routeId;
	}

	public void setRouteId(int routeId) {
		this.routeId = routeId;
	}

	public boolean isReplaceContent() {
		return replaceContent;
	}

	public void setReplaceContent(boolean replaceContent) {
		this.replaceContent = replaceContent;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	public int getNumSmscId() {
		return numSmscId;
	}

	public void setNumSmscId(int numSmscId) {
		this.numSmscId = numSmscId;
	}

	public int getBackupSmscId() {
		return backupSmscId;
	}

	public void setBackupSmscId(int backupSmscId) {
		this.backupSmscId = backupSmscId;
	}

	public String getForceSenderNum() {
		return forceSenderNum;
	}

	public void setForceSenderNum(String forceSenderNum) {
		this.forceSenderNum = forceSenderNum;
	}

	public String getForceSenderAlpha() {
		return forceSenderAlpha;
	}

	public void setForceSenderAlpha(String forceSenderAlpha) {
		this.forceSenderAlpha = forceSenderAlpha;
	}

	public String getExpiredOn() {
		return expiredOn;
	}

	public void setExpiredOn(String expiredOn) {
		this.expiredOn = expiredOn;
	}

	public int getSmsLength() {
		return smsLength;
	}

	public void setSmsLength(int smsLength) {
		this.smsLength = smsLength;
	}

	public boolean isRefund() {
		return refund;
	}

	public void setRefund(boolean refund) {
		this.refund = refund;
	}

	public String getRegSender() {
		return regSender;
	}

	public void setRegSender(String regSender) {
		this.regSender = regSender;
	}

	public int getRegSmscId() {
		return regSmscId;
	}

	public void setRegSmscId(int regSmscId) {
		this.regSmscId = regSmscId;
	}

	public int getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}

	public String getMsgAppender() {
		return msgAppender;
	}

	public void setMsgAppender(String msgAppender) {
		this.msgAppender = msgAppender;
	}

	public String getSourceAppender() {
		return sourceAppender;
	}

	public void setSourceAppender(String sourceAppender) {
		this.sourceAppender = sourceAppender;
	}

	public Map<String, String> getReplaceContentMap() {
		return replaceContentMap;
	}

	public void setReplaceContentMap(Map<String, String> replaceContentMap) {
		this.replaceContentMap = replaceContentMap;
	}

	public String getSenderReplFrom() {
		return senderReplFrom;
	}

	public void setSenderReplFrom(String senderReplFrom) {
		this.senderReplFrom = senderReplFrom;
	}

	public String getSenderReplTo() {
		return senderReplTo;
	}

	public void setSenderReplTo(String senderReplTo) {
		this.senderReplTo = senderReplTo;
	}

	public int getRegGroupId() {
		return regGroupId;
	}

	public void setRegGroupId(int regGroupId) {
		this.regGroupId = regGroupId;
	}

	public String toString() {
		return "RouteOpt: routeid=" + routeId + ",isReplaceContent=" + replaceContent + ",NumSmscId=" + numSmscId
				+ ",BackupSmscId=" + backupSmscId + ",RegisteredSmscId=" + regSmscId + ",forceSenderAlpha="
				+ forceSenderAlpha + ",forceSenderNum=" + forceSenderNum;
	}
}
