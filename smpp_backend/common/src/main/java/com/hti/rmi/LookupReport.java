/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.rmi;


import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 *
 * Lookup Reporting Object Shared Between websmpp & HLR server
 */
@Entity
@Table(name="look_up_report")
public class LookupReport implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int cc;
	private String country;
	private String operator;
	private long hlrid;
	private String responseid;
	private long number;
	private String status;
	private String networkCode;
	private String error;
	private String errorCode;
	private String submitTime;
	private String doneTime;
	private String username;
	private String parameter;
	private String batchId;
	private String isPorted;
	private String isRoaming;
	private String portedOperater;
	private String roamingCountry;
	private String roamingOperator;
	private String portedNNC;
	private String roamingNNC;
	private String roamingCC;
	private String imsi;
	private String msc;

	public String getMsc() {
		return msc;
	}

	public void setMsc(String msc) {
		this.msc = msc;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public int getCc() {
		return cc;
	}

	public void setCc(int cc) {
		this.cc = cc;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public long getHlrid() {
		return hlrid;
	}

	public void setHlrid(long hlrid) {
		this.hlrid = hlrid;
	}

	public String getResponseid() {
		return responseid;
	}

	public void setResponseid(String responseid) {
		this.responseid = responseid;
	}

	public long getNumber() {
		return number;
	}

	public void setNumber(long number) {
		this.number = number;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNetworkCode() {
		return networkCode;
	}

	public void setNetworkCode(String networkCode) {
		this.networkCode = networkCode;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public String getDoneTime() {
		return doneTime;
	}

	public void setDoneTime(String doneTime) {
		this.doneTime = doneTime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getIsPorted() {
		return isPorted;
	}

	public void setIsPorted(String isPorted) {
		this.isPorted = isPorted;
	}

	public String getIsRoaming() {
		return isRoaming;
	}

	public void setIsRoaming(String isRoaming) {
		this.isRoaming = isRoaming;
	}

	public String getPortedOperater() {
		return portedOperater;
	}

	public void setPortedOperater(String portedOperater) {
		this.portedOperater = portedOperater;
	}

	public String getRoamingCountry() {
		return roamingCountry;
	}

	public void setRoamingCountry(String roamingCountry) {
		this.roamingCountry = roamingCountry;
	}

	public String getRoamingOperator() {
		return roamingOperator;
	}

	public void setRoamingOperator(String roamingOperator) {
		this.roamingOperator = roamingOperator;
	}

	public String getPortedNNC() {
		return portedNNC;
	}

	public void setPortedNNC(String portedNNC) {
		this.portedNNC = portedNNC;
	}

	public String getRoamingNNC() {
		return roamingNNC;
	}

	public void setRoamingNNC(String roamingNNC) {
		this.roamingNNC = roamingNNC;
	}

	public String getRoamingCC() {
		return roamingCC;
	}

	public void setRoamingCC(String roamingCC) {
		this.roamingCC = roamingCC;
	}
}
