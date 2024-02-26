package com.hti.smpp.common.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;

public class SendAttachmentRequest {
	String senderEmail;
	String receiverEmail;
	String senderFirstName;
	String senderLastName;
	@Schema(hidden = true)
	MultipartFile attachment;

public MultipartFile getAttachment() {
	return attachment;
}

public void setAttachment(MultipartFile attachment) {
	this.attachment = attachment;
}
public String getSenderEmail() {
	return senderEmail;
}
public void setSenderEmail(String senderEmail) {
	this.senderEmail = senderEmail;
}
public String getReceiverEmail() {
	return receiverEmail;
}
public void setReceiverEmail(String receiverEmail) {
	this.receiverEmail = receiverEmail;
}
public String getSenderFirstName() {
	return senderFirstName;
}
public void setSenderFirstName(String senderFirstName) {
	this.senderFirstName = senderFirstName;
}
public String getSenderLastName() {
	return senderLastName;
}
public void setSenderLastName(String senderLastName) {
	this.senderLastName = senderLastName;
}


	
}
