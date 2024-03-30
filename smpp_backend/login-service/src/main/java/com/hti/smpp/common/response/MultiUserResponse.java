package com.hti.smpp.common.response;

import java.util.List;

import com.hti.smpp.common.user.dto.MultiUserEntry;

public class MultiUserResponse {
	  private String systemId;
	    private int multiUserId;
	    private List<MultiUserEntry> multiUserEntries;

	    // Constructors, getters, and setters

	    public MultiUserResponse(String systemId, int multiUserId, List<MultiUserEntry> multiUserEntries) {
	        this.systemId = systemId;
	        this.multiUserId = multiUserId;
	        this.multiUserEntries = multiUserEntries;
	    }

		public String getSystemId() {
			return systemId;
		}

		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		public int getMultiUserId() {
			return multiUserId;
		}

		public void setMultiUserId(int multiUserId) {
			this.multiUserId = multiUserId;
		}

		public List<MultiUserEntry> getMultiUserEntries() {
			return multiUserEntries;
		}

		public void setMultiUserEntries(List<MultiUserEntry> multiUserEntries) {
			this.multiUserEntries = multiUserEntries;
		}

	    
	    
}
