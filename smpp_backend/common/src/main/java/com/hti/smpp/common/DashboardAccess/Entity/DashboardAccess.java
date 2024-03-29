package com.hti.smpp.common.DashboardAccess.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="dashboard_access")
public class DashboardAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "msg_status", length = 255)
    private Boolean msgStatus;

    @Column(name = "country_wise_sub", length = 255)
    private Boolean countryWiseSub;

    @Column(name = "sender_wise_sub", length = 255)
    private Boolean senderWiseSub;

    @Column(name = "user_wise_st", length = 255)
    private Boolean userWiseSt;

    @Column(name = "smsc_wise_sub", length = 255)
    private Boolean smscWiseSub;

    @Column(name = "smsc_wise_dvy", length = 255)
    private Boolean smscWiseDvy;

    @Column(name = "smsc_wise_spam", length = 255)
    private Boolean smscWiseSpam;

    @Column(name = "user_wise_spam", length = 255)
    private Boolean userWiseSpam;

    @Column(name = "user_ID", unique = true)
    private Integer userId;

    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Boolean getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(Boolean msgStatus) {
		this.msgStatus = msgStatus;
	}

	public Boolean getCountryWiseSub() {
		return countryWiseSub;
	}

	public void setCountryWiseSub(Boolean countryWiseSub) {
		this.countryWiseSub = countryWiseSub;
	}

	public Boolean getSenderWiseSub() {
		return senderWiseSub;
	}

	public void setSenderWiseSub(Boolean senderWiseSub) {
		this.senderWiseSub = senderWiseSub;
	}

	public Boolean getUserWiseSt() {
		return userWiseSt;
	}

	public void setUserWiseSt(Boolean userWiseSt) {
		this.userWiseSt = userWiseSt;
	}

	public Boolean getSmscWiseSub() {
		return smscWiseSub;
	}

	public void setSmscWiseSub(Boolean smscWiseSub) {
		this.smscWiseSub = smscWiseSub;
	}

	public Boolean getSmscWiseDvy() {
		return smscWiseDvy;
	}

	public void setSmscWiseDvy(Boolean smscWiseDvy) {
		this.smscWiseDvy = smscWiseDvy;
	}

	public Boolean getSmscWiseSpam() {
		return smscWiseSpam;
	}

	public void setSmscWiseSpam(Boolean smscWiseSpam) {
		this.smscWiseSpam = smscWiseSpam;
	}

	public Boolean getUserWiseSpam() {
		return userWiseSpam;
	}

	public void setUserWiseSpam(Boolean userWiseSpam) {
		this.userWiseSpam = userWiseSpam;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

    
}
