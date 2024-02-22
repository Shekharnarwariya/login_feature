package com.hti.smpp.common.email;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.hti.smpp.common.exception.NotFoundException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Component
public class SendEmail {

private static final Logger logger = LoggerFactory.getLogger(SendEmail.class);
	

	@Autowired
    private JavaMailSender javaMailSender;
	
	@Autowired
    private MailProperties mailProperties;

	private void sendEmail(String email, String from, String content, String subject, String[] attachmentPaths,
			boolean IsCC) throws MessagingException, UnsupportedEncodingException {
		
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String to = email;
        InternetAddress[] address;
		if (to.contains(",")) {
			StringTokenizer emails = new StringTokenizer(to, ",");
			address = new InternetAddress[emails.countTokens()];
			int i = 0;
			while (emails.hasMoreTokens()) {
				String emailID = emails.nextToken();
				if (emailID.contains("@")) {
					address[i] = new InternetAddress(emailID);
					i++;
				}
			}
		} else {
			address = new InternetAddress[1];
			address[0] = new InternetAddress(to);
		}
        helper.setTo(address);
        
        helper.setFrom(new InternetAddress(from, from));
//      helper.setFrom(mailProperties.getMailId());
        
        if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[mailProperties.getMailCC().length];
			for (String cc : mailProperties.getMailCC()) {
				ccaddress[0] = new InternetAddress(cc);
			}
			helper.setCc(ccaddress);
		}
//        if (IsCC && mailProperties.getMailCC() != null) {
//            helper.setCc(mailProperties.getMailCC());
//        }
        
        helper.setSubject(subject);
        helper.setSentDate(new Date());
        helper.setText(content, true);
        
        // Attachments
        for (String attachmentPath : attachmentPaths) {
            File attachment = new File(attachmentPath);
            if (attachment.exists()) {
                helper.addAttachment(attachment.getName(), attachment);
            } else {
                logger.error("Attachment not found: " + attachment.getName());
                throw new NotFoundException("Attachment not found: " + attachment.getName());
            }
        }
        
        // Inline images
        addInlineImage(helper, "headerimg", "images/header.jpg");
        addInlineImage(helper, "footerimg", "images/footer.jpg");
        addInlineImage(helper, "footer2img", "images/footer_2.png");
        addInlineImage(helper, "lebanonimg", "images/lebanon.png");
        addInlineImage(helper, "uaeimg", "images/uae.png");
        addInlineImage(helper, "contactimg", "images/contact.jpg");
        
        try {
            javaMailSender.send(mimeMessage);
        } catch (Exception ex) {
            System.out.println(to + ": " + subject);
            ex.printStackTrace();
        }

	}
	
	private void addInlineImage(MimeMessageHelper helper, String contentId, String imagePath)
            throws MessagingException {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            helper.addInline(contentId, imageFile);
        } else {
            logger.error("Image not found: " + imageFile.getName());
            throw new NotFoundException("Image not found: " + imageFile.getName());
        }
    }
}
