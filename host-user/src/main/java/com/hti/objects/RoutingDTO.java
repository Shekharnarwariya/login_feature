/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.objects;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class RoutingDTO implements Serializable {
	private String username;
	private int networkId;
	private String smsc; // final route
	private int groupId;
	private String numsmsc;
	private String forceSIDNum;
	private String forceSIDAlpha;
	private String expiry; // to set the pdu expiry
	private double cost;
	private boolean hlr;
	private double hlrCost;
	private int smsLength; // for the routes which has message length restriction
	private String backupSmsc; // if Configured route has temporary issue than this route will be used
	private boolean refund; // in the case of non delivered messages
	private int codeLength; // for the routes which will add some code i.e. 4521 in msg content
	private Set<String> registerSenderId;
	private String registerSmsc;
	private int regGroupId; // group id of register sender routes
	private boolean replaceContent;
	private Map<String, String> replacement;
	private String sourceAppender; // text to append at begin/last of sourceId
	private String contentAppender; // text to append at begin/last of content
	private String routedSmsc; // initial route
	private Map<String, String> senderReplacement;
	private boolean mnp;
	private String numberLength;
	private boolean mms;

	public RoutingDTO() {
	}

	public RoutingDTO(int networkId, int groupId, double cost, boolean hlr, double hlrCost, String forceSIDNum,
			String forceSIDAlpha, String expiry, int smsLength, boolean refund, int codeLength,
			Set<String> registerSenderId, boolean replaceContent, Map<String, String> replacement,
			String sourceAppender, String contentAppender, int regGroupId, boolean mnp, String numberLength) {
		this.networkId = networkId;
		this.groupId = groupId;
		this.cost = cost;
		this.hlr = hlr;
		this.hlrCost = hlrCost;
		this.forceSIDNum = forceSIDNum;
		this.forceSIDAlpha = forceSIDAlpha;
		this.expiry = expiry;
		this.smsLength = smsLength;
		this.refund = refund;
		this.codeLength = codeLength;
		this.registerSenderId = registerSenderId;
		this.replaceContent = replaceContent;
		this.replacement = replacement;
		this.sourceAppender = sourceAppender;
		this.contentAppender = contentAppender;
		this.regGroupId = regGroupId;
		this.mnp = mnp;
		this.numberLength = numberLength;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public Set<String> getRegisterSenderId() {
		return registerSenderId;
	}

	public void setRegisterSenderId(Set<String> registerSenderId) {
		this.registerSenderId = registerSenderId;
	}

	public String getRegisterSmsc() {
		return registerSmsc;
	}

	public void setRegisterSmsc(String registerSmsc) {
		this.registerSmsc = registerSmsc;
	}

	public int getCodeLength() {
		return codeLength;
	}

	public void setCodeLength(int codeLength) {
		this.codeLength = codeLength;
	}

	public boolean isRefund() {
		return refund;
	}

	public void setRefund(boolean refund) {
		this.refund = refund;
	}

	public int getSmsLength() {
		return smsLength;
	}

	public String getBackupSmsc() {
		return backupSmsc;
	}

	public void setBackupSmsc(String backupSmsc) {
		this.backupSmsc = backupSmsc;
	}

	public void setSmsLength(int smsLength) {
		this.smsLength = smsLength;
	}

	public boolean isHlr() {
		return hlr;
	}

	public void setHlr(boolean hlr) {
		this.hlr = hlr;
	}

	public double getHlrCost() {
		return hlrCost;
	}

	public void setHlrCost(double hlrCost) {
		this.hlrCost = hlrCost;
	}

	public String getForceSIDAlpha() {
		return forceSIDAlpha;
	}

	public void setForceSIDAlpha(String forceSIDAlpha) {
		this.forceSIDAlpha = forceSIDAlpha;
	}

	public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getSmsc() {
		return smsc;
	}

	public void setSmsc(String smsc) {
		this.smsc = smsc;
	}

	public String getNumsmsc() {
		return numsmsc;
	}

	public void setNumsmsc(String numsmsc) {
		this.numsmsc = numsmsc;
	}

	public String getForceSIDNum() {
		return forceSIDNum;
	}

	public void setForceSIDNum(String forceSIDNum) {
		this.forceSIDNum = forceSIDNum;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public boolean isReplaceContent() {
		return replaceContent;
	}

	public void setReplaceContent(boolean replaceContent) {
		this.replaceContent = replaceContent;
	}

	public Map<String, String> getReplacement() {
		return replacement;
	}

	public void setReplacement(Map<String, String> replacement) {
		this.replacement = replacement;
	}

	public String getSourceAppender() {
		return sourceAppender;
	}

	public void setSourceAppender(String sourceAppender) {
		this.sourceAppender = sourceAppender;
	}

	public String getContentAppender() {
		return contentAppender;
	}

	public void setContentAppender(String contentAppender) {
		this.contentAppender = contentAppender;
	}

	public String getRoutedSmsc() {
		return routedSmsc;
	}

	public void setRoutedSmsc(String routedSmsc) {
		this.routedSmsc = routedSmsc;
	}

	public Map<String, String> getSenderReplacement() {
		return senderReplacement;
	}

	public void setSenderReplacement(Map<String, String> senderReplacement) {
		this.senderReplacement = senderReplacement;
	}

	public int getRegGroupId() {
		return regGroupId;
	}

	public void setRegGroupId(int regGroupId) {
		this.regGroupId = regGroupId;
	}

	public boolean isMnp() {
		return mnp;
	}

	public void setMnp(boolean mnp) {
		this.mnp = mnp;
	}

	public String getNumberLength() {
		return numberLength;
	}

	public void setNumberLength(String numberLength) {
		this.numberLength = numberLength;
	}

	public boolean isMms() {
		return mms;
	}

	public void setMms(boolean mms) {
		this.mms = mms;
	}

	public String toString() {
		return "Routing: NetworkId=" + networkId + ", Smsc=" + smsc + ", Cost=" + cost + ", Hlr=" + hlr + ",HlrCost="
				+ hlrCost + ", replaceContent=" + replaceContent;
	}
}
