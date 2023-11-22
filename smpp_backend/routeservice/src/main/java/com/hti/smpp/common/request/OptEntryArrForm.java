package com.hti.smpp.common.request;

public class OptEntryArrForm {
	private int[] routeId;
	private int[] numSmscId;
	private int[] backupSmscId;
	private String[] forceSenderNum;
	private String[] forceSenderAlpha;
	private int[] smsLength;
	private boolean[] refund;
	private String[] regSender;
	private int[] regSmscId;
	private int[] regGroupId;
	private int[] codeLength;
	private String[] expiredOn;
	private boolean[] replaceContent;
	private String[] replacement;
	private String[] msgAppender;
	private String[] sourceAppender;
	private String[] senderReplFrom;
	private String[] senderReplTo;
	private int[] userId;
	// ---- optional -----------------
	private String criterionEntries; // all id fetched under selected criteria to view again
	private boolean schedule;
	private String scheduledOn;

	public int[] getRouteId() {
		return routeId;
	}

	public void setRouteId(int[] routeId) {
		this.routeId = routeId;
	}

	public boolean[] getReplaceContent() {
		return replaceContent;
	}

	public void setReplaceContent(boolean[] replaceContent) {
		this.replaceContent = replaceContent;
	}

	public String[] getReplacement() {
		return replacement;
	}

	public void setReplacement(String[] replacement) {
		this.replacement = replacement;
	}

	public int[] getNumSmscId() {
		return numSmscId;
	}

	public void setNumSmscId(int[] numSmscId) {
		this.numSmscId = numSmscId;
	}

	public int[] getBackupSmscId() {
		return backupSmscId;
	}

	public void setBackupSmscId(int[] backupSmscId) {
		this.backupSmscId = backupSmscId;
	}

	public String[] getForceSenderNum() {
		return forceSenderNum;
	}

	public void setForceSenderNum(String[] forceSenderNum) {
		this.forceSenderNum = forceSenderNum;
	}

	public String[] getForceSenderAlpha() {
		return forceSenderAlpha;
	}

	public void setForceSenderAlpha(String[] forceSenderAlpha) {
		this.forceSenderAlpha = forceSenderAlpha;
	}

	public int[] getSmsLength() {
		return smsLength;
	}

	public void setSmsLength(int[] smsLength) {
		this.smsLength = smsLength;
	}

	public boolean[] getRefund() {
		return refund;
	}

	public void setRefund(boolean[] refund) {
		this.refund = refund;
	}

	public String[] getRegSender() {
		return regSender;
	}

	public void setRegSender(String[] regSender) {
		this.regSender = regSender;
	}

	public int[] getRegSmscId() {
		return regSmscId;
	}

	public void setRegSmscId(int[] regSmscId) {
		this.regSmscId = regSmscId;
	}

	public int[] getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int[] codeLength) {
		this.codeLength = codeLength;
	}

	public String[] getExpiredOn() {
		return expiredOn;
	}

	public void setExpiredOn(String[] expiredOn) {
		this.expiredOn = expiredOn;
	}

	public String[] getMsgAppender() {
		return msgAppender;
	}

	public void setMsgAppender(String[] msgAppender) {
		this.msgAppender = msgAppender;
	}

	public String[] getSourceAppender() {
		return sourceAppender;
	}

	public void setSourceAppender(String[] sourceAppender) {
		this.sourceAppender = sourceAppender;
	}

	public int[] getUserId() {
		return userId;
	}

	public int[] getRegGroupId() {
		return regGroupId;
	}

	public void setRegGroupId(int[] regGroupId) {
		this.regGroupId = regGroupId;
	}

	public void setUserId(int[] userId) {
		this.userId = userId;
	}

	public String[] getSenderReplFrom() {
		return senderReplFrom;
	}

	public void setSenderReplFrom(String[] senderReplFrom) {
		this.senderReplFrom = senderReplFrom;
	}

	public String[] getSenderReplTo() {
		return senderReplTo;
	}

	public void setSenderReplTo(String[] senderReplTo) {
		this.senderReplTo = senderReplTo;
	}

	public String getCriterionEntries() {
		return criterionEntries;
	}

	public void setCriterionEntries(String criterionEntries) {
		this.criterionEntries = criterionEntries;
	}

	public boolean isSchedule() {
		return schedule;
	}

	public void setSchedule(boolean schedule) {
		this.schedule = schedule;
	}

	public String getScheduledOn() {
		return scheduledOn;
	}

	public void setScheduledOn(String scheduledOn) {
		this.scheduledOn = scheduledOn;
	}

}
