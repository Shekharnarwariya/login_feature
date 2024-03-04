package com.hti.smpp.common.service;


import org.springframework.web.multipart.MultipartFile;


public interface FileAttachmentSenderService {
	public void sendEmailWithAttachment(String username,MultipartFile attachment,String sendAttachmentRequest);	
}
