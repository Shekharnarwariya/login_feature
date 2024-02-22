package com.hti.smpp.common.email;

import java.util.Arrays;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "load")
public class MailProperties {

	
	private String mailHost;
	private int smtpport;
	private String protocol = "smtp";
	private String mailId;
    private String mailPassword;
    private String[] mailCC; // If you have CC recipients
    private String mailTo;
    private String supportId;
    private String routeId;
    private String financeId;
    private String headerImagePath;
    private String footerImagePath;
    private String footerImage2Path;
    private String lebanonImagePath;
    private String uaeImagePath;
    private String contactImagePath;
    
    
    
	public MailProperties() {
		super();
	}

	public MailProperties(String mailHost, int smtpport, String protocol, String mailId, String mailPassword,
			String[] mailCC, String mailTo, String supportId, String routeId, String financeId, String headerImagePath,
			String footerImagePath, String footerImage2Path, String lebanonImagePath, String uaeImagePath,
			String contactImagePath) {
		super();
		this.mailHost = mailHost;
		this.smtpport = smtpport;
		this.protocol = protocol;
		this.mailId = mailId;
		this.mailPassword = mailPassword;
		this.mailCC = mailCC;
		this.mailTo = mailTo;
		this.supportId = supportId;
		this.routeId = routeId;
		this.financeId = financeId;
		this.headerImagePath = headerImagePath;
		this.footerImagePath = footerImagePath;
		this.footerImage2Path = footerImage2Path;
		this.lebanonImagePath = lebanonImagePath;
		this.uaeImagePath = uaeImagePath;
		this.contactImagePath = contactImagePath;
	}

	@Override
	public String toString() {
		return "MailProperties [mailHost=" + mailHost + ", smtpport=" + smtpport + ", protocol=" + protocol
				+ ", mailId=" + mailId + ", mailPassword=" + mailPassword + ", mailCC=" + Arrays.toString(mailCC)
				+ ", mailTo=" + mailTo + ", supportId=" + supportId + ", routeId=" + routeId + ", financeId="
				+ financeId + ", headerImagePath=" + headerImagePath + ", footerImagePath=" + footerImagePath
				+ ", footerImage2Path=" + footerImage2Path + ", lebanonImagePath=" + lebanonImagePath
				+ ", uaeImagePath=" + uaeImagePath + ", contactImagePath=" + contactImagePath + "]";
	}
	
	public String getMailHost() {
		return mailHost;
	}
	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}
	public int getSmtpport() {
		return smtpport;
	}
	public void setSmtpport(int smtpport) {
		this.smtpport = smtpport;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getMailId() {
		return mailId;
	}
	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	public String getMailPassword() {
		return mailPassword;
	}
	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}
	public String[] getMailCC() {
		return mailCC;
	}
	public void setMailCC(String[] mailCC) {
		this.mailCC = mailCC;
	}
	public String getMailTo() {
		return mailTo;
	}
	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}
	public String getSupportId() {
		return supportId;
	}
	public void setSupportId(String supportId) {
		this.supportId = supportId;
	}
	public String getRouteId() {
		return routeId;
	}
	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}
	public String getFinanceId() {
		return financeId;
	}
	public void setFinanceId(String financeId) {
		this.financeId = financeId;
	}
	public String getHeaderImagePath() {
		return headerImagePath;
	}
	public void setHeaderImagePath(String headerImagePath) {
		this.headerImagePath = headerImagePath;
	}
	public String getFooterImagePath() {
		return footerImagePath;
	}
	public void setFooterImagePath(String footerImagePath) {
		this.footerImagePath = footerImagePath;
	}
	public String getFooterImage2Path() {
		return footerImage2Path;
	}
	public void setFooterImage2Path(String footerImage2Path) {
		this.footerImage2Path = footerImage2Path;
	}
	public String getLebanonImagePath() {
		return lebanonImagePath;
	}
	public void setLebanonImagePath(String lebanonImagePath) {
		this.lebanonImagePath = lebanonImagePath;
	}
	public String getUaeImagePath() {
		return uaeImagePath;
	}
	public void setUaeImagePath(String uaeImagePath) {
		this.uaeImagePath = uaeImagePath;
	}
	public String getContactImagePath() {
		return contactImagePath;
	}
	public void setContactImagePath(String contactImagePath) {
		this.contactImagePath = contactImagePath;
	}
    
    
    
    
}
