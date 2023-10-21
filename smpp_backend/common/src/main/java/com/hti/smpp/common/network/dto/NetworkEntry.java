package com.hti.smpp.common.network.dto;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "network")

public class NetworkEntry implements Serializable {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "country")
	private String country;
	@Column(name = "operator")
	private String operator;
	@Column(name = "cc", length = 4)
	private int cc;
	@Column(name = "mcc", length = 3)
	private String mcc;
	@Column(name = "mnc")
	private String mnc;
	@Column(name = "prefix", columnDefinition = "LONGTEXT")
	private String prefix;
	@Column(name = "num_length", updatable = false)
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
