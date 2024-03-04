package com.hti.smpp.common.service.impl;

import java.io.File;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.JsonProcessingError;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.SendAttachmentRequest;
import com.hti.smpp.common.service.FileAttachmentSenderService;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.repository.ProfessionEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

@Service
public class FileAttachmentSenderServiceImpl implements FileAttachmentSenderService {
	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private UserEntryRepository userEntryRepository;

	@Autowired
	private ProfessionEntryRepository professionEntryRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	public FileAttachmentSenderServiceImpl(JavaMailSender javaMailSender) {
		this.javaMailSender = javaMailSender;
	}

	String emailSubject = "Confirmation: Successful Completion of Action";

	String Emailtemplate = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "<head>\r\n"
			+ "    <meta charset=\"UTF-8\">\r\n"
			+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n"
			+ "    <title>Success Report</title>\r\n" + "</head>\r\n" + "\r\n"
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
			+ "                    your recent activity.</p>\r\n" + "            </div>\r\n" + "        </div>\r\n"
			+ "        <p style=\"color: #666666; line-height: 1.6; text-align: center;\">If you have any questions or need further\r\n"
			+ "            assistance, please feel free to contact us.</p>\r\n"
			+ "        <p style=\"color: #666666; line-height: 1.6; text-align: center;\">Best regards,<br> [Your Organization]</p>\r\n"
			+ "    </div>\r\n" + "</body>\r\n" + "\r\n" + "</html>";

	private Logger logger = LoggerFactory.getLogger(FileAttachmentSenderServiceImpl.class);

	@Override
	public void sendEmailWithAttachment(String username, MultipartFile attachment, String sendAttachmentRequest) {

		Optional<UserEntry> userOptional = userEntryRepository.findBySystemId(username);

		UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));

		Optional<UserEntry> masterOptional = userEntryRepository.findBySystemId(user.getMasterId());

		UserEntry master = userOptional.orElseThrow(() -> new NotFoundException(messageResourceBundle
				.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { user.getMasterId() })));

		Optional<ProfessionEntry> professionOptional = professionEntryRepository.findByUserId(user.getId());

		ProfessionEntry profession = professionOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { user.getId() })));

		Optional<ProfessionEntry> professionOptionalmaster = professionEntryRepository.findByUserId(master.getId());

		ProfessionEntry professionmaster = professionOptional.orElseThrow(() -> new NotFoundException(
				messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { master.getId() })));

		if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {

			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));
		}
		SendAttachmentRequest emailRequest;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			emailRequest = objectMapper.readValue(sendAttachmentRequest, SendAttachmentRequest.class);
			emailRequest.setAttachment(attachment);
		} catch (JsonProcessingException e) {
			throw new JsonProcessingError("Json processing error");
		} catch (Exception e) {
			throw new InternalServerException("Failed to upload file!!");
		}

		String from = IConstants.SUPPORT_EMAIL[0];
		if (emailRequest.getSenderEmail() == null) {
			from = emailRequest.getSenderEmail();
		} else if (profession.getDomainEmail() != null) {
			from = profession.getDomainEmail();
		} else if (professionmaster.getDomainEmail() != null) {
			from = professionmaster.getDomainEmail();
		} else {
			logger.info(username + "[" + user.getId() + "]" + " DomainEmail[" + master.getSystemId() + "] Not Found");
		}
		try {
			send(emailRequest.getReceiverEmail(), from, Emailtemplate, emailSubject, emailRequest.getAttachment(), false);
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void send(String email, String from, String content, String subject, MultipartFile attachment, boolean IsCC)
			throws AddressException, MessagingException, Exception {
		String host = IConstants.mailHost;
		String to = email;
		final String pass = IConstants.mailPassword;
		final String mailAuthUser = IConstants.mailId;
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.port", IConstants.smtpPort + "");
		Session mailSession = Session.getDefaultInstance(props);
		Message message = new MimeMessage(mailSession);
		message.setFrom(new InternetAddress(from, from));
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
		message.setRecipients(Message.RecipientType.TO, address);
		if (IsCC) {
			InternetAddress[] ccaddress = new InternetAddress[IConstants.CC_EMAIL.length];
			for (String cc : IConstants.CC_EMAIL) {
				ccaddress[0] = new InternetAddress(cc);
			}
			message.setRecipients(Message.RecipientType.CC, ccaddress);
		}
		message.setSubject(subject);
		message.setSentDate(new Date());
		Multipart multipart = new MimeMultipart();
		try {
			// first part (the html) & Coverage Attachment
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(content, "text/html");
			if (attachment != null && !attachment.isEmpty()) {
				BodyPart attachmentBodyPart = new MimeBodyPart();
				// Convert MultipartFile to DataSource
				DataSource source = new ByteArrayDataSource(attachment.getBytes(), attachment.getContentType());
				attachmentBodyPart.setDataHandler(new DataHandler(source));
				attachmentBodyPart.setFileName(attachment.getOriginalFilename());
				multipart.addBodyPart(attachmentBodyPart);
			}
			// second part (the image)
			File fi = new File(IConstants.FORMAT_DIR + "images//header.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<headerimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("Header Image not exists: " + fi.getName());
			}
			// third part (the image)
			fi = new File(IConstants.FORMAT_DIR + "images//footer.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<footerimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("Footer Image not exists: " + fi.getName());
			}
		} catch (Exception ex) {
			logger.error(email + ": " + subject, ex.fillInStackTrace());
		}
		message.setContent(multipart);
		Transport.send(message, mailAuthUser, pass);
	}
}
