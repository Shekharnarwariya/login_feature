package com.hti.smpp.common.dto;

public class Network {

	 private int id;
	    private String country;
	    private String operator;
	    private String cc;
	    private String mcc;
	    private String mnc;
	    
	    
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

	    
	    
}
