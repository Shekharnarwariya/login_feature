package com.hti.smpp.common.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public class MccMncForm {
	
	private int id;
    private String country;
    private String operator;
    private String cc;
    private String mcc;
    private String mnc;
    private String prefix;
    @Schema(hidden = true)
    private MultipartFile listfile;
    private String checkCountry;
    private String checkMcc;
    private String checkMnc;
	public MccMncForm() {
		super();
	}
	public MccMncForm(int id, String country, String operator, String cc, String mcc, String mnc, String prefix,
			MultipartFile listfile, String checkCountry, String checkMcc, String checkMnc) {
		super();
		this.id = id;
		this.country = country;
		this.operator = operator;
		this.cc = cc;
		this.mcc = mcc;
		this.mnc = mnc;
		this.prefix = prefix;
		this.listfile = listfile;
		this.checkCountry = checkCountry;
		this.checkMcc = checkMcc;
		this.checkMnc = checkMnc;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getMcc() {
		return mcc;
	}
	public void setMcc(String mcc) {
		this.mcc = mcc;
	}
	public String getMnc() {
		return mnc;
	}
	public void setMnc(String mnc) {
		this.mnc = mnc;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public MultipartFile getListfile() {
		return listfile;
	}
	public void setListfile(MultipartFile listfile) {
		this.listfile = listfile;
	}
	public String getCheckCountry() {
		return checkCountry;
	}
	public void setCheckCountry(String checkCountry) {
		this.checkCountry = checkCountry;
	}
	public String getCheckMcc() {
		return checkMcc;
	}
	public void setCheckMcc(String checkMcc) {
		this.checkMcc = checkMcc;
	}
	public String getCheckMnc() {
		return checkMnc;
	}
	public void setCheckMnc(String checkMnc) {
		this.checkMnc = checkMnc;
	}
    
}
