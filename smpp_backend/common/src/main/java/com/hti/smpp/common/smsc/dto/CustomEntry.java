package com.hti.smpp.common.smsc.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "specialsmscsetting")
public class CustomEntry {
	@Id
	@Column(name = "smsc_id", unique = true, nullable = false)
	private int smscId;
	@Column(name = "length")
	private int sourceLength;
	@Column(name = "LSTon")
	private int lston;
	@Column(name = "LSNpi")
	private int lsnpi;
	@Column(name = "GSTon")
	private int gston;
	@Column(name = "GSNpi")
	private int gsnpi;

	public int getSmscId() {
		return smscId;
	}

	public void setSmscId(int smscId) {
		this.smscId = smscId;
	}

	public int getSourceLength() {
		return sourceLength;
	}

	public void setSourceLength(int sourceLength) {
		this.sourceLength = sourceLength;
	}

	public int getLston() {
		return lston;
	}

	public void setLston(int lston) {
		this.lston = lston;
	}

	public int getLsnpi() {
		return lsnpi;
	}

	public void setLsnpi(int lsnpi) {
		this.lsnpi = lsnpi;
	}

	public int getGston() {
		return gston;
	}

	public void setGston(int gston) {
		this.gston = gston;
	}

	public int getGsnpi() {
		return gsnpi;
	}

	public void setGsnpi(int gsnpi) {
		this.gsnpi = gsnpi;
	}

	public String toString() {
		return "CustomSmscEntry:smscId=" + smscId + ",SourceLength=" + sourceLength;
	}
}
