package com.hti.smpp.common.dto;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class MisDTO extends SmscInDTO {

	private String responseid;
	private int sub_ston;
	private int sub_snpi;
	private int sub_dton;
	private int sub_dnpi;
	private String delivertime;
	private String status;
	private String errcode;

	public String getResponseid() {
		return responseid;
	}

	public void setResponseid(String responseid) {
		this.responseid = responseid;
	}

	public int getSub_ston() {
		return sub_ston;
	}

	public void setSub_ston(int sub_ston) {
		this.sub_ston = sub_ston;
	}

	public int getSub_snpi() {
		return sub_snpi;
	}

	public void setSub_snpi(int sub_snpi) {
		this.sub_snpi = sub_snpi;
	}

	public int getSub_dton() {
		return sub_dton;
	}

	public void setSub_dton(int sub_dton) {
		this.sub_dton = sub_dton;
	}

	public int getSub_dnpi() {
		return sub_dnpi;
	}

	public void setSub_dnpi(int sub_dnpi) {
		this.sub_dnpi = sub_dnpi;
	}

	public String getDelivertime() {
		return delivertime;
	}

	public void setDelivertime(String delivertime) {
		this.delivertime = delivertime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
	}

}
