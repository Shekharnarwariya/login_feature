package com.hti.smpp.common.request;

public class CustomReportForm {
	private String smin;
	private String shour;
	private String sday;
	private String smonth;
	private String syear;
	private String ssec;
	private String mode;
	private String status;
	private String paidAmount;
	private String esec;
	private String emin;
	private String ehour;
	private String eday;
	private String emonth;
	private String eyear;
	private String senderId;
	private String senderRepl;
	private String destinationNumber;
	private String messageStatus;
	private String reportType;
	private String template;
	private String clientId;
	private String smscName;
	private String reroute;
	private String clientName;
	private String totalSmsSend;
	private String paidSms;
	private String smsVolume;
	private String country;
	private String operator;
	private String messageId;
	private String fromMsgId;
	private String toMsgId;
	private String check_A;
	private String check_B;
	private String check_C;
	private String check_D;
	private String check_E;
	private String check_F;
	private String check_G;
	private String groupBy;
	private String[] usernames;
	private String[] smscnames;
	private String campaign;
	private String campaignType;
	private String content;
	private String contentType;
	private int bsfmRule;
	private double delay;

	public String getCampaignType() {
		return campaignType;
	}

	public void setCampaignType(String campaignType) {
		this.campaignType = campaignType;
	}

	public String getCampaign() {
		return campaign;
	}

	public void setCampaign(String campaign) {
		this.campaign = campaign;
	}

	public String[] getUsernames() {
		return usernames;
	}

	public void setUsernames(String[] usernames) {
		this.usernames = usernames;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public String getReroute() {
		return reroute;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setReroute(String reroute) {
		this.reroute = reroute;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public void setSsec(String ssec) {
		this.ssec = ssec;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public void setSmin(String smin) {
		this.smin = smin;
	}

	public void setShour(String shour) {
		this.shour = shour;
	}

	public void setSday(String sday) {
		this.sday = sday;
	}

	public void setSmonth(String smonth) {
		this.smonth = smonth;
	}

	public void setSyear(String syear) {
		this.syear = syear;
	}

	public void setEsec(String esec) {
		this.esec = esec;
	}

	public void setEmin(String emin) {
		this.emin = emin;
	}

	public void setEhour(String ehour) {
		this.ehour = ehour;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public void setClientName(String clientName) {
		// System.out.println("clientname sett "+clientName);
		this.clientName = clientName;
	}

	public void setSmscName(String smscName) {
		// System.out.println("smscName sett "+smscName);
		this.smscName = smscName;
	}

	public void setEday(String eday) {
		this.eday = eday;
	}

	public void setEmonth(String emonth) {
		this.emonth = emonth;
	}

	public void setEyear(String eyear) {
		this.eyear = eyear;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public void setDestinationNumber(String destinationNumber) {
		this.destinationNumber = destinationNumber;
	}

	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public void setMode(String mode) {
		this.mode = mode;
		// System.out.println("set mode ::::::"+mode);
	}

	public void setPaidAmount(String paidAmount) {
		this.paidAmount = paidAmount;
	}

	public void setTotalSmsSend(String totalSmsSend) {
		this.totalSmsSend = totalSmsSend;
	}

	public void setPaidSms(String paidSms) {
		this.paidSms = paidSms;
	}

	public void setSmsVolume(String smsVolume) {
		this.smsVolume = smsVolume;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCheck_E() {
		return check_E;
	}

	public void setCheck_E(String check_E) {
		this.check_E = check_E;
	}

	public void setCheck_A(String check_A) {
		this.check_A = check_A;
	}

	public void setCheck_B(String check_B) {
		this.check_B = check_B;
	}

	public void setCheck_C(String check_C) {
		this.check_C = check_C;
	}

	public void setCheck_D(String check_D) {
		this.check_D = check_D;
	}

	public void setCheck_F(String check_F) {
		this.check_F = check_F;
	}

	public void setCheck_G(String check_G) {
		this.check_G = check_G;
	}

	////////////////////////////////////////////////////////////////////////////
	public String getCheck_G() {
		return check_G;
	}

	public String getCheck_F() {
		return check_F;
	}

	public String getCheck_C() {
		return check_C;
	}

	public String getCheck_D() {
		return check_D;
	}

	public String getCheck_A() {
		return check_A;
	}

	public String getCheck_B() {
		return check_B;
	}
	////////////////////////////////////////////////////////////////////////////

	public String getCountry() {
		return country;
	}

	public String getReportType() {
		return reportType;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getSmscName() {
		return smscName;
	}

	public String getSsec() {
		return ssec;
	}

	public String getSmin() {
		return smin;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getShour() {
		return shour;
	}

	public String getSday() {
		return sday;
	}

	public String getSmonth() {
		return smonth;
	}

	public String getSyear() {
		return syear;
	}

	public String getEsec() {
		return esec;
	}

	public String getEmin() {
		return emin;
	}

	public String getEhour() {
		return ehour;
	}

	public String getEday() {
		return eday;
	}

	public String getEmonth() {
		return emonth;
	}

	public String getEyear() {
		return eyear;
	}

	public String getSenderId() {
		return senderId;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public String getMessageStatus() {
		return messageStatus;
	}

	public String getStartDate() {
		return syear + "-" + smonth + "-" + sday + " " + shour + ":" + smin + ":01";
	}

	public String getEndDate() {
		return eyear + "-" + emonth + "-" + eday + " " + ehour + ":" + emin + ":59";
	}

	public String getTemplate() {
		return template;
	}

	public String getMode() {
		// System.out.println("get mode ::::::"+mode);
		return mode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaidAmount() {
		return paidAmount;
	}

	public String getTotalSmsSend() {
		return totalSmsSend;
	}

	public String getPaidSms() {
		return paidSms;
	}

	public String getSmsVolume() {
		return smsVolume;
	}

	public String[] getSmscnames() {
		return smscnames;
	}

	public void setSmscnames(String[] smscnames) {
		this.smscnames = smscnames;
	}

	public String getFromMsgId() {
		return fromMsgId;
	}

	public void setFromMsgId(String fromMsgId) {
		this.fromMsgId = fromMsgId;
	}

	public String getToMsgId() {
		return toMsgId;
	}

	public void setToMsgId(String toMsgId) {
		this.toMsgId = toMsgId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getSenderRepl() {
		return senderRepl;
	}

	public void setSenderRepl(String senderRepl) {
		this.senderRepl = senderRepl;
	}

	public int getBsfmRule() {
		return bsfmRule;
	}

	public void setBsfmRule(int bsfmRule) {
		this.bsfmRule = bsfmRule;
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
	}
}
