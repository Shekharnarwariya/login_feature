package com.hti.smpp.common.response;

import java.util.Collection;

import com.hti.smpp.common.sales.dto.SalesEntry;

/**
 * Represents a view containing details of a sales entry and a collection of managers.
 * Used for displaying sales entry information along with associated manager details.
 */

public class ViewSalesEntry {
	
	private SalesEntry seller;
	private Collection<SalesEntry> managers;
	
	public ViewSalesEntry() {
		super();
	}

	public ViewSalesEntry(SalesEntry seller, Collection<SalesEntry> managers) {
		super();
		this.seller = seller;
		this.managers = managers;
	}

	public SalesEntry getSeller() {
		return seller;
	}

	public void setSeller(SalesEntry seller) {
		this.seller = seller;
	}

	public Collection<SalesEntry> getManagers() {
		return managers;
	}

	public void setManagers(Collection<SalesEntry> managers) {
		this.managers = managers;
	}
}
