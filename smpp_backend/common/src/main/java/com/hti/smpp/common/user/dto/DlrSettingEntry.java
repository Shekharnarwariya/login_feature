package com.hti.smpp.common.user.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entity class representing user-specific delivery receipt (DLR) settings with JPA annotations.
 */
@Entity
@Table(name = "user_dlr_setting")
public class DlrSettingEntry implements Serializable {
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	private int userId;
	@Column(name = "restrictDLR")
	private boolean restrictDlr;
	@Column(name = "DLRthroughWEB")
	private boolean webDlr;
	@Column(name = "url")
	private String webUrl;
	@Column(name = "reverse_src")
	private boolean reverseSrc;
	@Column(name = "custom_gmt")
	private String customGmt;
	@Column(name = "nnc_dlr")
	private boolean nncDlr;
	@Column(name = "fixed_code")
	private boolean fixedCode;
	@Column(name = "accepted")
	private boolean accepted;
	@Column(name = "enforce_dlr")
	private boolean enforceDlr;
	@Column(name = "web_dlr_param")
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
