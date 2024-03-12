package com.hti.network.dto;

import java.io.Serializable;

public class NetworkEntry implements Serializable {
	private int id;
	private String country;
	private String operator;
	private int cc;
	private String mcc;
	private String mnc;
	private String prefix;
	private String numberLength;

	public NetworkEntry() {
	}

	public NetworkEntry(String country, String operator, String mcc, String mnc, int cc, String prefix) {
		this.country = country;
		this.operator = operator;
		this.mcc = mcc;
		this.mnc = mnc;
		this.cc = cc;
		this.prefix = prefix;
	}

	public NetworkEntry(int id, String country, String operator, String mcc, String mnc) {
		this.id = id;
		this.country = country;
		this.operator = operator;
		this.mcc = mcc;
		this.mnc = mnc;
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

	public int getCc() {
		return cc;
	}

	public void setCc(int cc) {
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

	public String getNumberLength() {
		return numberLength;
	}

	public void setNumberLength(String numberLength) {
		this.numberLength = numberLength;
	}

	public String toString() {
		return "network: id=" + id + ",cc=" + cc + ",country=" + country + ",operator=" + operator + ",mcc=" + mcc
				+ ",mnc=" + mnc + ",prefix=" + prefix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NetworkEntry entry = (NetworkEntry) o;
		return id == entry.id && (country == null ? entry.country == null : country.equals(entry.country))
				&& (operator == null ? entry.operator == null : operator.equals(entry.operator)) && (cc == entry.cc)
				&& (mcc == null ? entry.mcc == null : mcc.equals(entry.mcc))
				&& (mnc == null ? entry.mnc == null : mnc.equals(entry.mnc))
				&& (prefix == null ? entry.prefix == null : prefix.equals(entry.prefix));
	}
}
