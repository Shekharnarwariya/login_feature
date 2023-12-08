package com.hti.smpp.common.smsc.dto;

import java.util.List;

public class SmscEntryExt {
	private SmscEntry smscEntry;
	private List<StatusEntry> statusEntryList;
	private StatusEntry statusEntry;

	public SmscEntryExt(SmscEntry smscEntry) {
		this.smscEntry = smscEntry;
	}

	public SmscEntry getSmscEntry() {
		return smscEntry;
	}

	public void setSmscEntry(SmscEntry smscEntry) {
		this.smscEntry = smscEntry;
	}

	public List<StatusEntry> getStatusEntryList() {
		return statusEntryList;
	}

	public void setStatusEntryList(List<StatusEntry> statusEntryList) {
		this.statusEntryList = statusEntryList;
	}

	public StatusEntry getStatusEntry() {
		return statusEntry;
	}

	public void setStatusEntry(StatusEntry statusEntry) {
		this.statusEntry = statusEntry;
	}
}
