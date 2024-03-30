package com.hti.smpp.common.alertThreads;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hti.smpp.common.dto.Network;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.user.dto.UserEntryExt;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

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
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

@Component
public class AlertThread implements Runnable {

	private final DataBaseOpration service;

	@Autowired
	public AlertThread(DataBaseOpration service) {
		System.out.println("***** Price Change Alert Thread Started ******* ");
		this.service = service;
	}

	private Logger logger = LoggerFactory.getLogger(AlertThread.class);
	private boolean stop = false;
	private boolean isSleep;
	private boolean sleep;
	private Thread thread = null;
	private Map<String, String> custom_subject = new HashMap<String, String>();
	private Map<Integer, LocalModel> UserLocalEntry = new HashMap<Integer, LocalModel>();
	private Map<String, Integer> UserIdentity = new HashMap<String, Integer>();

	private class LocalModel {
		private String systemId;
		private String masterId;
		private String currency;
		private String domainEmail;
		private String coverageEmail;

		public String getSystemId() {
			return systemId;
		}

		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		public String getMasterId() {
			return masterId;
		}

		public void setMasterId(String masterId) {
			this.masterId = masterId;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public String getDomainEmail() {
			return domainEmail;
		}

		public void setDomainEmail(String domainEmail) {
			this.domainEmail = domainEmail;
		}

		public String getCoverageEmail() {
			return coverageEmail;
		}

		public void setCoverageEmail(String coverageEmail) {
			this.coverageEmail = coverageEmail;
		}
	}

	public void startThread() {
		thread = new Thread(this, "AlertThread");
		thread.start();
	}

	@Override
	public void run() {
		while (!stop) {
			if (GlobalVars.MASTER_CLIENT) {
				sleep = true;
				try {
					loadUserCache();
					checkCustomSettings();
					checkForPriceChange();
					if (sleep) {
						System.out.println("<-- Alert Thread Sleeping --> ");
						try {
							isSleep = true;
							Thread.sleep(IConstants.ALERT_INTERVAL * 60 * 1000); // 30 minutes
							isSleep = false;
						} catch (InterruptedException ex) {
							System.out.println("<-- Alert Thread Interuppted --> ");
						}
					} else {
						System.out.println("<-- Alert Thread Not Sleeping --> ");
					}
				} catch (Exception e) {
					logger.error("", e.fillInStackTrace());
				}
				UserLocalEntry.clear();
			} else {
				try {
					Thread.sleep(1 * 60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("***** Alert Thread Stopped ******* ");
	}

	public void check() {
		logger.info("<-- Command to check Price Changes --> ");
		if (isSleep) {
			System.out.println("<-- Interrupting Alert Thread --> ");
			thread.interrupt();
			// System.out.println("After Interrupt Status --> " + thread.getState());
		} else {
			System.out.println("<-- Alert Thread Already Processing --> ");
			sleep = false;
		}
	}

	private void checkCustomSettings() {
		try {
			custom_subject.clear();
			custom_subject = service.checkCustomSettings();
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		}
	}

	private void loadUserCache() {
		String system_id = null;
		try {
			Map<Integer, UserEntryExt> usermap = service.listUserEntries();
			for (Map.Entry<Integer, UserEntryExt> entry : usermap.entrySet()) {
				system_id = entry.getValue().getUserEntry().getSystemId();
				LocalModel localModel = new LocalModel();
				String coverage_email = entry.getValue().getWebMasterEntry().getCoverageEmail();
				if (coverage_email != null && coverage_email.contains("@") && coverage_email.contains(".")) {
					localModel.setCoverageEmail(coverage_email);
				}
				String domail_email = entry.getValue().getProfessionEntry().getDomainEmail();
				if (domail_email != null && domail_email.contains("@") && domail_email.contains(".")) {
					localModel.setDomainEmail(domail_email);
				}
				String currency = entry.getValue().getUserEntry().getCurrency();
				if (currency != null && currency.length() == 3) {
					localModel.setCurrency(currency);
				}
				localModel.setSystemId(entry.getValue().getUserEntry().getSystemId());
				localModel.setMasterId(entry.getValue().getUserEntry().getMasterId());
				UserLocalEntry.put(entry.getKey(), localModel);
				UserIdentity.put(entry.getValue().getUserEntry().getSystemId(), entry.getKey());
			}
		} catch (Exception e) {
			logger.error(system_id, e);
		}
	}

	private void checkForPriceChange() {
		System.out.println("***** Checking For Price Change Alert ******* ");
		Map<Integer, List<RouteEntryExt>> alert_map = new HashMap<Integer, List<RouteEntryExt>>();
		boolean schedule = false;
		boolean proceed = false;
		try {
			Map<Integer, List<RouteEntryExt>> map = service.checkForPriceChange();
			if (map.isEmpty()) {
				System.out.println("***** No Price Change Found ******* ");
				map = service.checkForPriceChangeSch();
				if (map.isEmpty()) {
					System.out.println("***** No Price Change Schedules Found ******* ");
				} else {
					proceed = true;
					schedule = true;
				}
			} else {
				proceed = true;
			}
			if (proceed) {
				Map<Integer, Network> network_map = service.getNetworkRecord(new HashSet<Integer>(map.keySet()));
				for (Map.Entry<Integer, List<RouteEntryExt>> entry : map.entrySet()) {
					int prefix = entry.getKey();
					List<RouteEntryExt> route_list = entry.getValue();
					while (!route_list.isEmpty()) {
						RouteEntryExt route = route_list.remove(0);
						int user_id = route.getBasic().getUserId();
						String currency = "&euro;";
						if (UserLocalEntry.containsKey(user_id)) {
							currency = UserLocalEntry.get(user_id).getCurrency();
							if (currency != null) {
								if (GlobalVars.currencies.containsKey(currency)) {
									currency = GlobalVars.currencies.get(currency);
								}
								if (currency == null || currency.length() == 0) {
									currency = "&euro;";
								}
							}
						}
						route.setCurrency(currency);
						if (prefix == 0) {
							route.setCountry("Deafult");
							route.setOperator("Default");
							route.setMcc("-");
							route.setMnc("-");
						} else {
							Network network = null;
							if (network_map.containsKey(prefix)) {
								network = network_map.get(prefix);
							}
							if (network == null) {
								System.out.println(user_id + " -> Network Record Not Found For :" + prefix);
								route.setCountry("Default");
								route.setOperator("Default");
								route.setMcc("-");
								route.setMnc("-");
							} else {
								route.setCountry(network.getCountry());
								route.setOperator(network.getOperator());
								route.setMcc(network.getMcc());
								route.setMnc(network.getMnc());
							}
						}
						List<RouteEntryExt> list = null;
						if (alert_map.containsKey(user_id)) {
							list = alert_map.get(user_id);
						} else {
							list = new ArrayList<RouteEntryExt>();
						}
						list.add(route);
						alert_map.put(user_id, list);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("", ex.fillInStackTrace());
		}
		if (!alert_map.isEmpty()) {
			Map<Integer, String> flag_map = new HashMap<Integer, String>();
			try {
				for (Map.Entry<Integer, List<RouteEntryExt>> entry : alert_map.entrySet()) {
					int user_id = entry.getKey();
					List<RouteEntryExt> list = entry.getValue();
					if (schedule) {
						logger.info("Creating Price Change Schedule Alert Email For " + user_id + " Entries: "
								+ list.size());
					} else {
						logger.info("Creating Price Change Alert Email For " + user_id + " Entries: " + list.size());
					}
					// sort list by country/Operator
					list.sort(Comparator.comparing(RouteEntryExt::getCountry, CASE_INSENSITIVE_ORDER)
							.thenComparing(RouteEntryExt::getOperator, CASE_INSENSITIVE_ORDER));
					String flag = "true";
					if (UserLocalEntry.containsKey(user_id) && UserLocalEntry.get(user_id).getCoverageEmail() != null) {
						String systemId = UserLocalEntry.get(user_id).getSystemId();
						String email = UserLocalEntry.get(user_id).getCoverageEmail();
						String currency = UserLocalEntry.get(user_id).getCurrency();
						if (currency == null || currency.length() != 3) {
							currency = "EUR";
						}
						String from = IConstants.ROUTE_EMAIL[0];
						int master_id = UserIdentity.get(UserLocalEntry.get(user_id).getMasterId());
						if (UserLocalEntry.get(user_id).getDomainEmail() != null) {
							from = UserLocalEntry.get(user_id).getDomainEmail();
						} else {
							if (UserLocalEntry.get(master_id).getDomainEmail() != null) {
								from = UserLocalEntry.get(master_id).getDomainEmail();
							} else {
								logger.info(
										systemId + "[" + user_id + "]" + " DomainEmail[" + master_id + "] Not Found");
							}
						}
						// String subject = "BroadNet - Prices Update";
						String subject = "Prices Update";
						// System.out.println("Custom Price Change Subject: " + custom_subject);
						if (custom_subject.containsKey(systemId)) {
							subject = custom_subject.get(systemId);
							if (Pattern.compile(Pattern.quote("[date]"), Pattern.CASE_INSENSITIVE).matcher(subject)
									.find()) {
								subject = subject.replaceAll("(?i)" + Pattern.quote("[Date]"),
										new SimpleDateFormat("dd-MM-yyyy").format(new Date()));
							}
							if (Pattern.compile(Pattern.quote("[time]"), Pattern.CASE_INSENSITIVE).matcher(subject)
									.find()) {
								subject = subject.replaceAll("(?i)" + Pattern.quote("[time]"),
										new SimpleDateFormat("HH:mm:ss").format(new Date()));
							}
							if (Pattern.compile(Pattern.quote("[Currency]"), Pattern.CASE_INSENSITIVE).matcher(subject)
									.find()) {
								subject = subject.replaceAll("(?i)" + Pattern.quote("[Currency]"),
										currency.toUpperCase());
							}
							logger.info(systemId + "[" + user_id + "]" + " Subject: " + subject);
						}
						if (schedule) {
							subject += " Scheduled";
						}
						try {
							String content = createPriceChangeContent(list, systemId, schedule);
							String[] coverage_report = new String[2];
							Map<Integer, RouteEntryExt> map_covergae = service.listCoverage(systemId, true, true);
							coverage_report[0] = getCoverageReportXLS(systemId, list, currency, map_covergae);
							coverage_report[1] = getCoverageReportPDF(systemId, list, currency, map_covergae);
							send(email, from, content, subject, coverage_report, false);
							logger.info("Price Change Alert[" + systemId + "] Sent from:" + from + " To:" + email);
						} catch (Exception ex) {
							flag = "error";
							ex.printStackTrace();
							System.out.println("catch run");
							logger.info(ex + " While Sending price Change Alert to " + systemId);
						}
					} else {
						flag = "error";
						logger.info(" Email Not Found For price Change Alert " + user_id);
					}
					// System.out.println(user + " Update Flag: " + list.size());
					while (!list.isEmpty()) {
						RouteEntryExt routing = list.remove(0);
						flag_map.put(routing.getBasic().getId(), flag);
					}
				}
				if (!flag_map.isEmpty()) {
					if (schedule) {
						service.updateRoutingFlagSch(flag_map);
					} else {
						service.updateRoutingFlag(flag_map);
					}
				}
			} catch (Exception ex) {
				logger.error("", ex.fillInStackTrace());
			}
		}
	}

	private void send(String email, String from, String content, String subject, String[] attachment, boolean IsCC)
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
			multipart.addBodyPart(messageBodyPart);
			// Attachment
			for (int i = 0; i < attachment.length; i++) {
				messageBodyPart = new MimeBodyPart();
				File attach = new File(attachment[i]);
				DataSource source = new FileDataSource(attach);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(attach.getName());
				multipart.addBodyPart(messageBodyPart);
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
			fi = new File(IConstants.FORMAT_DIR + "images//footer_2.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<footer2img>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("Footer_2 Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//lebanon.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<lebanonimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("Lebanon Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//uae.png");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<uaeimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("UAE Image not exists: " + fi.getName());
			}
			fi = new File(IConstants.FORMAT_DIR + "images//contact.jpg");
			if (fi.exists()) {
				messageBodyPart = new MimeBodyPart();
				DataSource fds = new FileDataSource(fi);
				messageBodyPart.setDataHandler(new DataHandler(fds));
				messageBodyPart.setHeader("Content-ID", "<contactimg>");
				messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				// add image to the multipart
				multipart.addBodyPart(messageBodyPart);
			} else {
				System.out.println("Contact Image not exists: " + fi.getName());
			}
		} catch (Exception ex) {
			logger.error(email + ": " + subject, ex.fillInStackTrace());
		}
		message.setContent(multipart);
		Transport.send(message, mailAuthUser, pass);
	}

	private String createPriceChangeContent(List<RouteEntryExt> list, String username, boolean schedule) {
		String htmlString = "<body>" + "<table >" + "<tr><td>" + "<table width='758'>" + "<tr>" + "<td>"
		// + header
				+ "</td>" + "</tr>" + "</table>" + "</td></tr>" + "<tr align='left'>"
				+ "<td style='font-family: Calibri,sans-serif;font-size: 11pt;'>"
				+ "<span style='color: #1F497D;'>Dear Valued Partner,</span>" + "<br><br>"
				+ "<span style='color: #1F497D;'>Please take note of the changes";
		if (schedule) {
			htmlString += " Scheduled as below";
		} else {
			htmlString += " effective immediately ( " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
					+ " [+02:00]) as below";
		}
		htmlString += " to your account <b>" + username + "</b>&nbsp;[" + IConstants.GATEWAY_NAME + " - "
				+ IConstants.SMPP_IP + "]</span>" + "</td>" + "</tr>" + "<tr align='left'>" + "<td>&nbsp;</td>"
				+ "</tr>" + "<tr><td>"
				+ "<table style='border-collapse: collapse;' cellspacing='0' cellpadding='0' border='0' align='left' summary=''>"
				+ "	<tr style='height: 40pt' align='center'>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>Country</span></strong></td>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>Operator</span></strong></td>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>MCC</span></strong></td>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>MNC</span></strong></td>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>Old&nbsp;Price</span></strong></td>"
				+ "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>New&nbsp;Price</span></strong></td>";
		htmlString += "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>EffectiveOn(GMT +2)</span></strong></td>";
		htmlString += "	    <td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: 1pt medium medium 1pt;border-style: solid none none solid;border-color: #7BA0CD -moz-use-text-color -moz-use-text-color #7BA0CD;background: #4F81BD none repeat scroll 0% 0%;padding: 0in 5.4pt;'><strong><span style='color: white;'>Remarks</span></strong></td>"
				+ "	</tr>";
		// Iterator itr = list.iterator();
		int i = 0;
		for (RouteEntryExt routingDTO : list) {
			String remarks = routingDTO.getRemarks();
			htmlString += "<tr style='height: 40pt' align='center'>";
			if (++i % 2 == 0) {
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getCountry() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getOperator() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getMcc() + "</td>";
				String mnc = routingDTO.getMnc();
				try {
					if (Integer.parseInt(routingDTO.getMnc()) == 0) {
						if (routingDTO.getOperator() != null && routingDTO.getOperator().equalsIgnoreCase("Rest")) {
							mnc = " ";
						}
					}
				} catch (Exception ex) {
				}
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ mnc + "</td>";
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getCurrency() + "&nbsp;" + routingDTO.getOldCost() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><b>"
						+ routingDTO.getCurrency() + "&nbsp;" + routingDTO.getCostStr() + "<b></td>";
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><b>"
						+ routingDTO.getBasic().getEditOn() + "<b></td>";
				if (remarks.equalsIgnoreCase("Increased")) {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: red'>"
							+ remarks + "&nbsp;&#9650;</span></td>";
				} else if (remarks.equalsIgnoreCase("Decreased")) {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: green'>"
							+ remarks + "&nbsp;&#9660;</span></td>";
				} else {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #DBE5F1 none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: green'>"
							+ remarks + "</span></td>";
				}
			} else {
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getCountry() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getOperator() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getMcc() + "</td>";
				String mnc = routingDTO.getMnc();
				try {
					if (Integer.parseInt(routingDTO.getMnc()) == 0) {
						if (routingDTO.getOperator() != null && routingDTO.getOperator().equalsIgnoreCase("Rest")) {
							mnc = " ";
						}
					}
				} catch (Exception ex) {
				}
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ mnc + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'>"
						+ routingDTO.getCurrency() + "&nbsp;" + routingDTO.getOldCost() + "</td>"
						+ "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><b>"
						+ routingDTO.getCurrency() + "&nbsp;" + routingDTO.getCostStr() + "<b></td>";
				htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><b>"
						+ routingDTO.getBasic().getEditOn() + "<b></td>";
				if (remarks.equalsIgnoreCase("Increased")) {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: red'>"
							+ remarks + "&nbsp;&#9650;</span></td>";
				} else if (remarks.equalsIgnoreCase("Decreased")) {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: green'>"
							+ remarks + "&nbsp;&#9660;</span></td>";
				} else {
					htmlString += "<td width=250 style='font-family: Calibri,sans-serif;font-size: 11pt;border-width: medium medium 1pt 1pt;border-style: none none solid solid;border-color: -moz-use-text-color -moz-use-text-color #7BA0CD #7BA0CD;background: #FFF none repeat scroll 0% 0%;padding: 0in 5.4pt;height: 4pt;'><span style='color: green'>"
							+ remarks + "</span></td>";
				}
			}
			htmlString += "</tr>";
		}
		htmlString += "</table>" + "</td></tr>" + "<tr><td>&nbsp;" + "</td></tr>" + "<tr><td>"
				+ "<table width='758' align='left'>" + "<tr>" + "<td>";
		// footer text
		try {
			htmlString += MultiUtility.readContent(IConstants.FORMAT_DIR + "email//PricingFooter.txt");
		} catch (Exception ex) {
			htmlString += "Kind Regards<br>Routing Team";
		}
		htmlString += "</td>" + "</tr>" + "</table>" + "</td></tr>" + "</table>" + "<br>" + "<br>" + "<br>" + "</body>";
		return htmlString;
	}

	private String getCoverageReportPDF(String username, List<RouteEntryExt> coverageList, String currency,
			Map<Integer, RouteEntryExt> map_covergae) throws DocumentException, FileNotFoundException, IOException {
		String filename = IConstants.WEBSMPP_EXT_DIR + "report//" + username + "_coverage.pdf";
		Document document = new Document(PageSize.A4, 5, 5, 35, 35);
		PdfWriter.getInstance(document, new FileOutputStream(filename));
		// ---Font Definitions------------------------
		Font font_header = new Font(Font.TIMES_ROMAN, 18, 1, Color.BLUE);
		Font font_headLine = new Font(Font.TIMES_ROMAN, 12, 1, Color.WHITE);
		Font font_footer = new Font(Font.COURIER, 10, 1, Color.BLUE);
		Font font_ConHead = new Font(Font.COURIER, 11, 1, Color.red);
		Font font_Content = new Font(Font.TIMES_ROMAN, 10, 1, Color.BLACK);
		// ---Font Definitions------------------------
		Image logo = Image.getInstance(IConstants.WEBSMPP_EXT_DIR + "images//logo.jpg");
		logo.setAlignment(Image.MIDDLE);
		logo.scaleToFit(30, 24);
		String report_Heading = "Current Pricing List";
		String footer_MSG = "";
		// -------------Set header & Footer------------------
		HeaderFooter header = new HeaderFooter(new Phrase(report_Heading, font_header), false);
		header.setAlignment(Element.ALIGN_LEFT);
		HeaderFooter footer = new HeaderFooter(new Phrase(footer_MSG + "         Page No. ", font_footer), true);
		footer.setAlignment(Element.ALIGN_CENTER);
		header.setBackgroundColor(Color.orange);
		document.setHeader(header);
		document.setFooter(footer);
		// -------------Set header & Footer------------------
		document.open();
		int NumColumns = 10;
		int sno = 1;
		username = "\tUsername :: " + username;
		// -----------------------------------------------
		PdfPTable head_line = new PdfPTable(2);
		head_line.getDefaultCell().setBorder(0);
		head_line.getDefaultCell().setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		head_line.getDefaultCell().setBackgroundColor(Color.GRAY);
		head_line.getDefaultCell().setVerticalAlignment(Element.ALIGN_JUSTIFIED);
		head_line.getDefaultCell().setPaddingBottom(10);
		head_line.setWidthPercentage(90);
		head_line.addCell(new Phrase(username, font_headLine));
		head_line.addCell(new Phrase(new Date().toString(), font_headLine));
		// -----------------------------------------------
		PdfPTable Under_line = new PdfPTable(1);
		Under_line.getDefaultCell().setBorder(0);
		Under_line.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		head_line.setWidthPercentage(90);
		Under_line.addCell("    ");
		// -----------------------------------------------
		PdfPTable datatable = new PdfPTable(NumColumns);
		int headerwidths[] = { 8, 10, 10, 8, 8, 10, 10, 10, 10, 10 }; // percentage
		datatable.setWidths(headerwidths);
		datatable.setWidthPercentage(90);
		datatable.getDefaultCell().setPaddingBottom(5);
		datatable.getDefaultCell().setBorderWidth(1);
		datatable.addCell(new Phrase(" S No.", font_ConHead));
		datatable.addCell(new Phrase(" Country ", font_ConHead));
		datatable.addCell(new Phrase(" Operator ", font_ConHead));
		datatable.addCell(new Phrase(" MCC ", font_ConHead));
		datatable.addCell(new Phrase(" MNC ", font_ConHead));
		datatable.addCell(new Phrase(" OldCost ", font_ConHead));
		datatable.addCell(new Phrase(" NewCost ", font_ConHead));
		datatable.addCell(new Phrase(" EffectiveOn(GMT +2) ", font_ConHead));
		datatable.addCell(new Phrase(" Currency ", font_ConHead));
		datatable.addCell(new Phrase(" Remarks ", font_ConHead));
		datatable.getDefaultCell().setGrayFill(1);
		datatable.setHeaderRows(1);
		for (RouteEntryExt entry : coverageList) {
			if (sno % 2 == 1) {
				datatable.getDefaultCell().setGrayFill(0.9f);
			}
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			datatable.addCell(new Phrase(sno + "", font_Content));
			datatable.addCell(new Phrase(entry.getCountry(), font_Content));
			datatable.addCell(new Phrase(entry.getOperator(), font_Content));
			datatable.addCell(new Phrase(entry.getMcc(), font_Content));
			datatable.addCell(new Phrase(mnc, font_Content));
			datatable.addCell(new Phrase(entry.getOldCost(), font_Content));
			datatable.addCell(new Phrase(entry.getCostStr(), font_Content));
			datatable.addCell(new Phrase(entry.getBasic().getEditOn(), font_Content));
			datatable.addCell(new Phrase(currency, font_Content));
			datatable.addCell(new Phrase(entry.getRemarks(), font_Content));
			sno = sno + 1;
			if (map_covergae.containsKey(entry.getBasic().getNetworkId())) {
				map_covergae.remove(entry.getBasic().getNetworkId());
			}
		}
		for (RouteEntryExt entry : map_covergae.values()) {
			if (sno % 2 == 1) {
				datatable.getDefaultCell().setGrayFill(0.9f);
			}
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			datatable.addCell(new Phrase(sno + "", font_Content));
			datatable.addCell(new Phrase(entry.getCountry(), font_Content));
			datatable.addCell(new Phrase(entry.getOperator(), font_Content));
			datatable.addCell(new Phrase(entry.getMcc(), font_Content));
			datatable.addCell(new Phrase(mnc, font_Content));
			datatable.addCell(new Phrase(String.valueOf(entry.getBasic().getCost()), font_Content));
			datatable.addCell(new Phrase(String.valueOf(entry.getBasic().getCost()), font_Content));
			datatable.addCell(new Phrase("No Change", font_Content));
			datatable.addCell(new Phrase(currency, font_Content));
			datatable.addCell(new Phrase("No Change", font_Content));
			sno = sno + 1;
		}
		// --------------------------------------------------Adding Tables to
		// Document-------------------
		document.add(head_line);
		document.add(Under_line);
		document.add(datatable);
		// --------------------------------------------------Adding Tables to
		// Document-------------------
		document.close();
		return filename;
	}

	private String getCoverageReportXLS(String username, List<RouteEntryExt> list, String currency,
			Map<Integer, RouteEntryExt> map_covergae) throws WriteException, IOException {
		Collection<RouteEntryExt> collection = sortAndFilter(list);
		String filename = IConstants.WEBSMPP_EXT_DIR + "report//" + username + "_coverage.xls";
		WritableFont courier = new WritableFont(WritableFont.createFont("Calibri"), 11, WritableFont.BOLD);
		WritableFont times = new WritableFont(WritableFont.createFont("Calibri"), 11);
		WritableCellFormat courierformat = new WritableCellFormat(courier);
		WritableCellFormat timesformat = new WritableCellFormat(times);
		courierformat.setAlignment(jxl.format.Alignment.CENTRE);
		courierformat.setBackground(Colour.GREY_25_PERCENT);
		courierformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
		timesformat.setAlignment(Alignment.LEFT);
		// timesformat.setBackground(Colour.LIGHT_GREEN);
		// timesformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.DARK_GREEN);
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
		WritableSheet sheet = workbook.createSheet(username + "(0)", 0);
		sheet.getSettings().setDefaultColumnWidth(25);
		sheet.addCell(new Label(0, 0, "Country", courierformat));
		sheet.addCell(new Label(1, 0, "Operator", courierformat));
		sheet.addCell(new Label(2, 0, "MCC", courierformat));
		sheet.addCell(new Label(3, 0, "MNC", courierformat));
		sheet.addCell(new Label(4, 0, "OldCost", courierformat));
		sheet.addCell(new Label(5, 0, "NewCost", courierformat));
		sheet.addCell(new Label(6, 0, "EffectiveOn(GMT +2)", courierformat));
		sheet.addCell(new Label(7, 0, "Currency", courierformat));
		sheet.addCell(new Label(8, 0, "Remarks", courierformat));
		int rowNum = 1;
		for (RouteEntryExt entry : collection) {
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			sheet.addCell(new Label(0, rowNum, entry.getCountry(), timesformat));
			sheet.addCell(new Label(1, rowNum, entry.getOperator(), timesformat));
			sheet.addCell(new Label(2, rowNum, entry.getMcc(), timesformat));
			sheet.addCell(new Label(3, rowNum, mnc, timesformat));
			sheet.addCell(new Label(4, rowNum, entry.getOldCost(), timesformat));
			sheet.addCell(new Label(5, rowNum, entry.getCostStr(), timesformat));
			sheet.addCell(new Label(6, rowNum, entry.getBasic().getEditOn(), timesformat));
			sheet.addCell(new Label(7, rowNum, currency, timesformat));
			sheet.addCell(new Label(8, rowNum, entry.getRemarks(), timesformat));
			rowNum++;
			if (map_covergae.containsKey(entry.getBasic().getNetworkId())) {
				map_covergae.remove(entry.getBasic().getNetworkId());
			}
		}
		for (RouteEntryExt entry : map_covergae.values()) {
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			sheet.addCell(new Label(0, rowNum, entry.getCountry(), timesformat));
			sheet.addCell(new Label(1, rowNum, entry.getOperator(), timesformat));
			sheet.addCell(new Label(2, rowNum, entry.getMcc(), timesformat));
			sheet.addCell(new Label(3, rowNum, mnc, timesformat));
			sheet.addCell(new Label(4, rowNum, String.valueOf(entry.getBasic().getCost()), timesformat));
			sheet.addCell(new Label(5, rowNum, String.valueOf(entry.getBasic().getCost()), timesformat));
			sheet.addCell(new Label(6, rowNum, "No Change", timesformat));
			sheet.addCell(new Label(7, rowNum, currency, timesformat));
			sheet.addCell(new Label(8, rowNum, "No Change", timesformat));
			rowNum++;
		}
		workbook.write();
		workbook.close();
		return filename;
	}

	public void stopThread() {
		logger.info("***** Alert Thread Stopping ******* ");
		sleep = false;
		stop = true;
		if (isSleep) {
			logger.info("<-- Interrupting Alert Thread --> ");
			thread.interrupt();
		}
	}

	private Collection<RouteEntryExt> sortAndFilter(List<RouteEntryExt> list) {
		Collections.sort(list, new SortByEffectiveOn());
		Map<Integer, RouteEntryExt> sort_map = new LinkedHashMap<Integer, RouteEntryExt>();
		for (RouteEntryExt ext : list) {
			sort_map.put(ext.getBasic().getNetworkId(), ext); // to replace old record with latest
		}
		return sort_map.values();
	}

	private class SortByEffectiveOn implements Comparator<RouteEntryExt> {
		// Sorting in ascending order
		public int compare(RouteEntryExt a, RouteEntryExt b) {
			return a.getBasic().getEditOn().compareTo(b.getBasic().getEditOn());
		}
	}
}
