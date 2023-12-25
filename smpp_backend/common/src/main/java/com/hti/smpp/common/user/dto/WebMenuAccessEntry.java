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
@Table(name = "web_menu_access")
public class WebMenuAccessEntry implements Serializable {
	@Id
	@Column(name = "user_id", unique = true, nullable = false)
	private int userId;
	@Column(name = "sms")
	private boolean messaging;
	@Column(name = "utility")
	private boolean utility;
	@Column(name = "report")
	private boolean report;
	@Column(name = "addbook")
	private boolean addbook;
	@Column(name = "template")
	private boolean template;
	@Column(name = "bsfm")
	private boolean bsfm;
	@Column(name = "routing")
	private boolean routing;
	@Column(name = "user")
	private boolean user;
	@Column(name = "sales")
	private boolean sales;
	@Column(name = "2way")
	private boolean twoWay;

	public WebMenuAccessEntry() {
	}

	public WebMenuAccessEntry(int userId) {
		this.userId = userId;
	}

	public WebMenuAccessEntry(int userId, boolean messaging, boolean utility, boolean report, boolean addbook,
			boolean template, boolean bsfm, boolean routing, boolean user, boolean sales, boolean twoWay) {
		this.userId = userId;
		this.messaging = messaging;
		this.utility = utility;
		this.report = report;
		this.addbook = addbook;
		this.template = template;
		this.bsfm = bsfm;
		this.routing = routing;
		this.user = user;
		this.sales = sales;
		this.twoWay = twoWay;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isMessaging() {
		return messaging;
	}

	public void setMessaging(boolean messaging) {
		this.messaging = messaging;
	}

	public boolean isUtility() {
		return utility;
	}

	public void setUtility(boolean utility) {
		this.utility = utility;
	}

	public boolean isReport() {
		return report;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

	public boolean isAddbook() {
		return addbook;
	}

	public void setAddbook(boolean addbook) {
		this.addbook = addbook;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public boolean isBsfm() {
		return bsfm;
	}

	public void setBsfm(boolean bsfm) {
		this.bsfm = bsfm;
	}

	public boolean isRouting() {
		return routing;
	}

	public void setRouting(boolean routing) {
		this.routing = routing;
	}

	public boolean isUser() {
		return user;
	}

	public void setUser(boolean user) {
		this.user = user;
	}

	public boolean isSales() {
		return sales;
	}

	public void setSales(boolean sales) {
		this.sales = sales;
	}
	
	public boolean isTwoWay() {
		return twoWay;
	}

	public void setTwoWay(boolean twoWay) {
		this.twoWay = twoWay;
	}

	public String toString() {
		return "WebMenuAccessEntry: userId=" + userId + ",messaging=" + messaging + ",report=" + report + ",addbook="
				+ addbook + ",template=" + template + ",utility=" + utility + ",bsfm=" + bsfm + ",routing=" + routing
				+ ",user=" + user + ",sales=" + sales + ",2way=" + twoWay;
	}
	

	
}
