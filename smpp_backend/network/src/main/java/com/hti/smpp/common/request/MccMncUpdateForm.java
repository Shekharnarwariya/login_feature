package com.hti.smpp.common.request;

public class MccMncUpdateForm {
	
	private int[] id;
    private String[] country;
    private String[] operator;
    private String[] cc;
    private String[] mcc;
    private String[] mnc;
    private String[] prefix;
    private int totalRecords;
	public MccMncUpdateForm() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MccMncUpdateForm(int[] id, String[] country, String[] operator, String[] cc, String[] mcc, String[] mnc,
			String[] prefix, int totalRecords) {
		super();
		this.id = id;
		this.country = country;
		this.operator = operator;
		this.cc = cc;
		this.mcc = mcc;
		this.mnc = mnc;
		this.prefix = prefix;
		this.totalRecords = totalRecords;
	}
	public int[] getId() {
		return id;
	}
	public void setId(int[] id) {
		this.id = id;
	}
	public String[] getCountry() {
		return country;
	}
	public void setCountry(String[] country) {
		this.country = country;
	}
	public String[] getOperator() {
		return operator;
	}
	public void setOperator(String[] operator) {
		this.operator = operator;
	}
	public String[] getCc() {
		return cc;
	}
	public void setCc(String[] cc) {
		this.cc = cc;
	}
	public String[] getMcc() {
		return mcc;
	}
	public void setMcc(String[] mcc) {
		this.mcc = mcc;
	}
	public String[] getMnc() {
		return mnc;
	}
	public void setMnc(String[] mnc) {
		this.mnc = mnc;
	}
	public String[] getPrefix() {
		return prefix;
	}
	public void setPrefix(String[] prefix) {
		this.prefix = prefix;
	}
	public int getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
	}
    
    

}
