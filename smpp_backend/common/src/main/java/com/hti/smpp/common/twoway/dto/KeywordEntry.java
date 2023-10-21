package com.hti.smpp.common.twoway.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name = "2way_keyword")
public class KeywordEntry {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(name = "user_id")
	private int userId;
	@Column(name = "prefix")
	private String prefix;
	@Column(name = "suffix", length = 10)
	private String suffix;
	@Column(name = "type")
	private String type;
	@Column(name = "short_code", length = 15)
	private String shortCode;
	@Column(name = "expiresOn")
	private String expiresOn;
	@Column(name = "reply")
	private boolean reply;
	@Column(name = "success_msg")
	private String replyMessage;
	@Column(name = "failed_msg")
	private String replyOnFailed;
	@Column(name = "expire_msg")
	private String replyOnExpire;
	@Column(name = "reply_sender")
	private String replySender;
	@Column(name = "alert_number")
	private String alertNumber;
	@Column(name = "alert_email")
	private String alertEmail;
	@Column(name = "alert_url")
	private String alertUrl;
	@Column(name = "createdOn", updatable = false)
	private String createdOn;
	@Column(name = "createdBy", updatable = false)
	private String createdBy;
	@Column(name = "sources")
	private String sources;
	@Transient
	private String systemId;

}
