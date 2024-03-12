package com.hti.objects;

import java.io.Serializable;

import com.logica.smpp.pdu.Request;

public class RoutePDU extends RoutingDTO implements Serializable {
	private Request requestPDU;
	private String HtiMsgId;
	private int sequence_no;
	private String sessionId;
	private int priority;
	private String time;
	private boolean deduct;
	private String originalSourceAddr;
	private String status;
	private boolean registerSender;
	private boolean ported;
	private String portedNNC;
	private PartDesription partDescription;
	private boolean walletFlag;
	private boolean smscContentReplacement = true; // Smsc Based Content Replacement
	private boolean rerouted;

	/** Creates a new instance of RoutePDU */
	public RoutePDU(Request getRequest, String getHtiMsgId, int getSquence_no, String sessionId, int priority) {
		this.requestPDU = getRequest;
		this.HtiMsgId = getHtiMsgId;
		this.sequence_no = getSquence_no;
		this.sessionId = sessionId;
		this.priority = priority;
	}

	public RoutePDU() {
	}

	public class PartDesription implements Serializable {
		private int total;
		private int partNumber;
		private int referenceNumber;

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public int getPartNumber() {
			return partNumber;
		}

		public void setPartNumber(int partNumber) {
			this.partNumber = partNumber;
		}

		public int getReferenceNumber() {
			return referenceNumber;
		}

		public void setReferenceNumber(int referenceNumber) {
			this.referenceNumber = referenceNumber;
		}
	}

	public PartDesription getPartDescription() {
		if (partDescription == null) {
			partDescription = new PartDesription();
		}
		return partDescription;
	}

	public void setPartDescription(PartDesription partDescription) {
		this.partDescription = partDescription;
	}

	public boolean isPorted() {
		return ported;
	}

	public void setPorted(boolean ported) {
		this.ported = ported;
	}

	public String getPortedNNC() {
		return portedNNC;
	}

	public void setPortedNNC(String portedNNC) {
		this.portedNNC = portedNNC;
	}

	public boolean isRegisterSender() {
		return registerSender;
	}

	public void setRegisterSender(boolean registerSender) {
		this.registerSender = registerSender;
	}

	public String getOriginalSourceAddr() {
		return originalSourceAddr;
	}

	public void setOriginalSourceAddr(String originalSourceAddr) {
		this.originalSourceAddr = originalSourceAddr;
	}

	public boolean isDeduct() {
		return deduct;
	}

	public void setDeduct(boolean deduct) {
		this.deduct = deduct;
	}

	public synchronized Request getRequestPDU() {
		return requestPDU;
	}

	public synchronized String getHtiMsgId() {
		return HtiMsgId;
	}

	public synchronized int getSequence_no() {
		return sequence_no;
	}

	public synchronized void setSequence_no(int getSequence_no) {
		sequence_no = getSequence_no;
	}

	public synchronized String getSessionId() {
		return sessionId;
	}

	public synchronized int getPriority() {
		return priority;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isWalletFlag() {
		return walletFlag;
	}

	public void setWalletFlag(boolean walletFlag) {
		this.walletFlag = walletFlag;
	}

	public String toString() {
		return "RoutePDU: msgid=" + HtiMsgId + ",sequence=" + sequence_no;
	}

	public boolean isSmscContentReplacement() {
		return smscContentReplacement;
	}

	public void setSmscContentReplacement(boolean smscContentReplacement) {
		this.smscContentReplacement = smscContentReplacement;
	}

	public boolean isRerouted() {
		return rerouted;
	}

	public void setRerouted(boolean rerouted) {
		this.rerouted = rerouted;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
