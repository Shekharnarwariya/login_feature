package com.hti.smpp.common.route.dto;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "route_opt")
public class OptionalRouteEntry implements Serializable {
	@Id
	@Column(name = "route_id", unique = true, nullable = false)
	private int routeId;
	@Column(name = "num_smsc_id")
	private int numSmscId;
	@Column(name = "backup_smsc_id")
	private int backupSmscId;
	@Column(name = "forceSIDNum")
	private String forceSenderNum;
	@Column(name = "forceSIDAlpha")
	private String forceSenderAlpha;
	@Column(name = "set_expiry")
	private String expiredOn = "0";
	@Column(name = "sms_length")
	private int smsLength;
	@Column(name = "refund")
	private boolean refund;
	@Column(name = "reg_sender_id")
	private String regSender;
	@Column(name = "reg_smsc_id")
	private int regSmscId;
	@Column(name = "reg_group_id")
	private int regGroupId;
	@Column(name = "code_length")
	private int codeLength;
	@Column(name = "isReplaceContent")
	private boolean replaceContent;
	@Column(name = "content_replace")
	private String replacement;
	@Column(name = "msgAppender")
	private String msgAppender;
	@Column(name = "sourceAppender")
	private String sourceAppender;
	@Column(name = "editby")
	private String editBy;
	@Column(name = "edit_on")
	private String editOn;
	@Column(name = "sender_repl_from")
	private String senderReplFrom;
	@Column(name = "sender_repl_to")
	private String senderReplTo;
	@Transient
	private Map<String, String> replaceContentMap;

	public OptionalRouteEntry(String editBy, String editOn) {
		this.editBy = editBy;
		this.editOn = editOn;
	}

	public OptionalRouteEntry() {
	}

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
