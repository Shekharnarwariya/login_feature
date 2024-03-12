package com.hti.user.dto;

import java.io.Serializable;

public class DlrSettingEntry implements Serializable {
	private int userId;
	private boolean restrictDlr;
	private boolean webDlr;
	private String webUrl;
	private boolean reverseSrc;
	private String customGmt;
	private boolean nncDlr;
	private boolean fixedCode;
	private boolean accepted;
	private boolean enforceDlr;
	private String webDlrParam;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isRestrictDlr() {
		return restrictDlr;
	}

	public void setRestrictDlr(boolean restrictDlr) {
		this.restrictDlr = restrictDlr;
	}

	public boolean isWebDlr() {
		return webDlr;
	}

	public void setWebDlr(boolean webDlr) {
		this.webDlr = webDlr;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String webUrl) {
		this.webUrl = webUrl;
	}

	public boolean isReverseSrc() {
		return reverseSrc;
	}

	public void setReverseSrc(boolean reverseSrc) {
		this.reverseSrc = reverseSrc;
	}

	public String getCustomGmt() {
		return customGmt;
	}

	public void setCustomGmt(String customGmt) {
		this.customGmt = customGmt;
	}

	public boolean isNncDlr() {
		return nncDlr;
	}

	public void setNncDlr(boolean nncDlr) {
		this.nncDlr = nncDlr;
	}

	public boolean isFixedCode() {
		return fixedCode;
	}

	public void setFixedCode(boolean fixedCode) {
		this.fixedCode = fixedCode;
	}

	public boolean isAccepted() {
		return accepted;
	}

	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}

	public boolean isEnforceDlr() {
		return enforceDlr;
	}

	public void setEnforceDlr(boolean enforceDlr) {
		this.enforceDlr = enforceDlr;
	}

	public String getWebDlrParam() {
		return webDlrParam;
	}

	public void setWebDlrParam(String webDlrParam) {
		this.webDlrParam = webDlrParam;
	}

	public String toString() {
		return "Dlr: Userid=" + userId + ",RestrictDlr=" + restrictDlr + ",WebDlr=" + webDlr;
	}
}
