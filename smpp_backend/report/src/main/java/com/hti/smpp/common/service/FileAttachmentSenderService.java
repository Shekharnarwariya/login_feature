package com.hti.smpp.common.service;


import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.request.SendAttachmentRequest;


public interface FileAttachmentSenderService {
	public void sendEmailWithAttachment(MultipartFile attachment,String sendAttachmentRequest);	
}
