package com.hti.smpp.common.response;

public class DeliveryDTO extends ReportBaseDTO {
	private String responseId;
	private String status;
	private String deliverOn;
	private int statusCount;
	private String category;
	private String bsfmRule;
	private int ruleCount;
	private String remarks;
	private String errCode;
	// ------ status names-------
	private int submitted;
	private int delivered;
	private int undelivered;
	private int pending;
	private int expired;
	private int others;
	// ------ latency ranges ----
	private int latency1; // 0-5
	private int latency2; // 6-15
	private int latency3; // 16-30
	private int latency4; // 31-45
	private int latency5; // 46-60
	private int latency6; // > 60
	private String latencyRange;
	private int latencyCount;
	// ------ Consumption ----
	private String currency;
	private double consumption;

	public DeliveryDTO(String msgid, String responseId, String route, String country, String operator,
			String destination, String sender, double cost, String date, String time, String status) {
		setMsgid(msgid);
		setRoute(route);
		setCountry(country);
		setCost(cost);
		setOperator(operator);
		setDestination(destination);
		setSender(sender);
		setDate(date);
		setTime(time);
		this.responseId = responseId;
		this.status = status;
	}

	public DeliveryDTO(String country, String operator, double cost, String date, String status, int statusCount) {
		setCountry(country);
		setOperator(operator);
		setCost(cost);
		setDate(date);
		this.status = status;
		this.statusCount = statusCount;
	}

	public DeliveryDTO(String sender, double cost, String date, String status, int statusCount) {
		setSender(sender);
		setCost(cost);
		setDate(date);
		this.status = status;
		this.statusCount = statusCount;
	}

	public DeliveryDTO(String country, String operator, double cost, String date, int submitted, int delivered) {
		setCountry(country);
		setOperator(operator);
		setCost(cost);
		setDate(date);
		this.submitted = submitted;
		this.delivered = delivered;
	}

	public DeliveryDTO(String country, String operator, String date) {
		setCountry(country);
		setOperator(operator);
		setDate(date);
	}

	public DeliveryDTO(String msgid, String country, String operator, String destination, String sender, double cost,
			String date, String time, String status) {
		setMsgid(msgid);
		setCountry(country);
		setOperator(operator);
		setCost(cost);
		setDestination(destination);
		setSender(sender);
		setDate(date);
		setTime(time);
		this.status = status;
	}

	public DeliveryDTO(String msgid, String destination, String sender, double cost, String date, String time,
			String status) {
		setMsgid(msgid);
		setCost(cost);
		setDestination(destination);
		setSender(sender);
		setDate(date);
		setTime(time);
		this.status = status;
	}

	public DeliveryDTO(String status, int statusCount) {
		this.status = status;
		this.statusCount = statusCount;
	}

	public DeliveryDTO(int latencyCount, String latencyRange) {
		this.latencyCount = latencyCount;
		this.latencyRange = latencyRange;
	}

	public DeliveryDTO() {
	}

	public String getDeliverOn() {
		return deliverOn;
	}

	public void setDeliverOn(String deliverOn) {
		this.deliverOn = deliverOn;
	}

	public int getUndelivered() {
		return undelivered;
	}

	public void setUndelivered(int undelivered) {
		this.undelivered = undelivered;
	}

	public int getPending() {
		return pending;
	}

	public void setPending(int pending) {
		this.pending = pending;
	}

	public int getSubmitted() {
		return submitted;
	}

	public void setSubmitted(int submitted) {
		this.submitted = submitted;
	}

	public int getDelivered() {
		return delivered;
	}

	public void setDelivered(int delivered) {
		this.delivered = delivered;
	}

	public String getResponseId() {
		return responseId;
	}

	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}

	public String getStatus() {
		return status;
	}

	public int getExpired() {
		return expired;
	}

	public void setExpired(int expired) {
		this.expired = expired;
	}

	public int getOthers() {
		return others;
	}

	public void setOthers(int others) {
		this.others = others;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(int statusCount) {
		this.statusCount = statusCount;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getLatency1() {
		return latency1;
	}

	public void setLatency1(int latency1) {
		this.latency1 = latency1;
	}

	public int getLatency2() {
		return latency2;
	}

	public void setLatency2(int latency2) {
		this.latency2 = latency2;
	}

	public int getLatency3() {
		return latency3;
	}

	public void setLatency3(int latency3) {
		this.latency3 = latency3;
	}

	public int getLatency4() {
		return latency4;
	}

	public void setLatency4(int latency4) {
		this.latency4 = latency4;
	}

	public int getLatency5() {
		return latency5;
	}

	public void setLatency5(int latency5) {
		this.latency5 = latency5;
	}

	public int getLatency6() {
		return latency6;
	}

	public void setLatency6(int latency6) {
		this.latency6 = latency6;
	}

	public String getLatencyRange() {
		return latencyRange;
	}

	public void setLatencyRange(String latencyRange) {
		this.latencyRange = latencyRange;
	}

	public int getLatencyCount() {
		return latencyCount;
	}

	public void setLatencyCount(int latencyCount) {
		this.latencyCount = latencyCount;
	}

	public String getBsfmRule() {
		return bsfmRule;
	}

	public void setBsfmRule(String bsfmRule) {
		this.bsfmRule = bsfmRule;
	}

	public int getRuleCount() {
		return ruleCount;
	}

	public void setRuleCount(int ruleCount) {
		this.ruleCount = ruleCount;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getConsumption() {
		return consumption;
	}

	public void setConsumption(double consumption) {
		this.consumption = consumption;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	@Override
	public String toString() {
		return "DeliveryDTO [responseId=" + responseId + ", status=" + status + ", deliverOn=" + deliverOn
				+ ", statusCount=" + statusCount + ", category=" + category + ", bsfmRule=" + bsfmRule + ", ruleCount="
				+ ruleCount + ", remarks=" + remarks + ", errCode=" + errCode + ", submitted=" + submitted
				+ ", delivered=" + delivered + ", undelivered=" + undelivered + ", pending=" + pending + ", expired="
				+ expired + ", others=" + others + ", latency1=" + latency1 + ", latency2=" + latency2 + ", latency3="
				+ latency3 + ", latency4=" + latency4 + ", latency5=" + latency5 + ", latency6=" + latency6
				+ ", latencyRange=" + latencyRange + ", latencyCount=" + latencyCount + ", currency=" + currency
				+ ", consumption=" + consumption + "]";
	}

}
