package com.hti.tw.filter;

public class FilterEntry {
	public boolean proceed;
	public String sources;

	public FilterEntry(boolean proceed, String sources) {
		this.proceed = proceed;
		this.sources = sources;
	}

	public String toString() {
		return "FilterEntry: proceed=" + proceed + ",sources=" + sources;
	}
}
