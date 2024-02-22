package com.hti.smpp.common.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.request.SendAttachmentRequest;
import com.hti.smpp.common.service.FileAttachmentSenderService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class FileAttachmentSenderServiceImpl implements FileAttachmentSenderService  {
	@Autowired
	private JavaMailSender javaMailSender;
	
	@Autowired
	public FileAttachmentSenderServiceImpl(JavaMailSender javaMailSender) {
		this.javaMailSender=javaMailSender;
	}
	
	String emailSubject="Confirmation: Successful Completion of Action";
	
	String Emailtemplate="<!DOCTYPE html>\r\n"
			+ "<html lang=\"en\">\r\n"
			+ "<head>\r\n"
			+ "    <meta charset=\"UTF-8\">\r\n"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
			+ "    <title>Success Report</title>\r\n"
			+ "</head>\r\n"
			+ "\r\n"
			+ "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;\">\r\n"
			+ "    <div class=\"container\"\r\n"
			+ "        style=\"max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">\r\n"
			+ "        <h1 style=\"color: #8dc165; text-align: center;\">Successful!!</h1>\r\n"
			+ "        <p style=\"color: #666666; line-height: 1.6; text-align: center;\">Hello <span\r\n"
			+ "                style=\"font-weight: bold; color: #666666;\">User</span>,</p>\r\n"
			+ "        <div class=\"report\" style=\"background-color: #f9f9f9; padding: 15px; border-radius: 5px;\">\r\n"
			+ "            <div class=\"section-content\" style=\"margin-left: 15px;\">\r\n"
			+ "                <p>This is to inform you that your recent action was successful. Congratulations!</p>\r\n"
			+ "                <p>We have attached a document below for your convenience. It contains detailed information related to\r\n"
			+ "                    your recent activity.</p>\r\n"
			+ "            </div>\r\n"
			+ "        </div>\r\n"
			+ "        <p style=\"color: #666666; line-height: 1.6; text-align: center;\">If you have any questions or need further\r\n"
			+ "            assistance, please feel free to contact us.</p>\r\n"
			+ "        <p style=\"color: #666666; line-height: 1.6; text-align: center;\">Best regards,<br> [Your Organization]</p>\r\n"
			+ "    </div>\r\n"
			+ "</body>\r\n"
			+ "\r\n"
			+ "</html>";
	
	private Logger logger=LoggerFactory.getLogger(FileAttachmentSenderServiceImpl.class);
	
	@Override
	public void sendEmailWithAttachment(MultipartFile attachment,String sendAttachmentRequest) {
		SendAttachmentRequest form;
		try {
		ObjectMapper objectMapper = new ObjectMapper();
		form=objectMapper.readValue(sendAttachmentRequest,SendAttachmentRequest.class);
		form.setAttachment(attachment);
		}catch(JsonProcessingException e) {
			throw new JsonProcessingError("Json processing error");
		}catch(Exception e) {
			throw new InternalServerException("Failed to upload file!!");
		}
		try {
			MimeMessage message=javaMailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message,true);
			helper.setTo(form.getReceiverEmail());
			helper.setFrom(form.getSenderEmail());
			helper.setText(Emailtemplate, true);
			helper.setSubject(emailSubject);
			if(form.getAttachment()!=null &&!form.getAttachment().isEmpty()) {
			helper.addAttachment(form.getAttachment().getOriginalFilename(), form.getAttachment());
			}
			javaMailSender.send(message);
			logger.info("Attachment sent to Email"+ form.getReceiverEmail());	
		}catch(MessagingException ex) {
			logger.error("Error while sending an Email "+form.getReceiverEmail());
		}catch(MailSendException e) {
			logger.error("error while sending mail");
			e.printStackTrace();
		}
	}
}
