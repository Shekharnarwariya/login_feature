/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.network.dto.NetworkEntry;
import com.hti.objects.AlertDTO;
import com.hti.user.dto.UserEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;
import com.hti.util.GoogleTest;
import com.hti.util.TextEncoder;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TCPIPConnection;
import com.logica.smpp.pdu.BindRequest;
import com.logica.smpp.pdu.BindTransmitter;
import com.logica.smpp.pdu.Response;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.UnbindResp;

/**
 *
 * @author Administrator
 */
public class PerformAlert implements Runnable {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");
	private AlertDTO alert;
	private String test_user;
	private String test_password;
	private Map<String, Integer> submitted_map = new HashMap<String, Integer>();
	private Map<String, Integer> status_map = new HashMap<String, Integer>();
	private boolean stop;
	private Thread thread;
	private boolean sleep;

	public PerformAlert(AlertDTO alert) {
		this.alert = alert;
		logger.info("PerformanceAlert[" + alert.getId() + "] Starting");
		getInternalUser();
	}

	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	public void setAlert(AlertDTO alert) {
		this.alert = alert;
	}

	@Override
	public void run() {
		while (!stop) {
			logger.info(" Alert: " + alert.getId() + " Waiting for Duration " + alert.getDuration() + " Minutes ");
			String back_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			submitted_map.clear();
			status_map.clear();
			sleep = true;
			try {
				Thread.sleep(alert.getDuration() * 60 * 1000);
			} catch (InterruptedException ex) {
			}
			sleep = false;
			if (!stop) {
				logger.info("Checking Performance for " + alert.getStatus() + " Alert: " + alert.getId());
				String sql = "select count(msg_id) as count,smsc,oprCountry,sender,status from report where";
				String current_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				sql += " time between '" + back_time + "' and '" + current_time + "' ";
				if (alert.getRoutes() != null && alert.getRoutes().length() > 0) {
					sql += "and smsc in('" + alert.getRoutes().replaceAll(",", "','") + "') ";
				}
				if (alert.getCountries() != null && alert.getCountries().length() > 0) {
					sql += "and oprCountry in('" + alert.getCountries().replaceAll(",", "','") + "') ";
				}
				sql += "group by smsc,oprCountry,sender,status";
				logger.info("Alert[" + alert.getId() + "]: " + sql);
				PreparedStatement statement = null;
				ResultSet rs = null;
				Connection connection = null;
				int submitted = 0, statusCounter = 0;
				try {
					connection = GlobalCache.connnection_pool_1.getConnection();
					statement = connection.prepareStatement(sql);
					rs = statement.executeQuery();
					while (rs.next()) {
						submitted = 0;
						statusCounter = 0;
						int count = rs.getInt("count");
						String status = rs.getString("status");
						String route = rs.getString("smsc");
						String network = rs.getString("oprCountry");
						String sender = rs.getString("sender");
						String key = route + "#" + network;
						if (alert.getSenders() != null && alert.getSenders().length() > 0) {
							boolean source_matched = false;
							for (String source_key : alert.getSenders().split(",")) {
								// System.out.println(source_key + " Matching Try: " + source);
								if (Pattern.compile(source_key).matcher(sender).find()) {
									logger.debug(alert.getId() + "-> " + source_key + " matched Source: " + sender);
									source_matched = true;
									break;
								}
							}
							if (source_matched) {
								key += "#" + sender;
								logger.info(alert.getId() + " -> " + key + " " + status + " count: " + count);
								if (status == null) {
									status = "QUEUED";
								}
								if (submitted_map.containsKey(key)) {
									submitted = (Integer) submitted_map.get(key);
								}
								submitted = submitted + count;
								submitted_map.put(key, submitted);
								if (alert.getStatus().equalsIgnoreCase(status)) {
									if (status_map.containsKey(key)) {
										statusCounter = (Integer) status_map.get(key);
									}
									statusCounter = statusCounter + count;
									status_map.put(key, statusCounter);
								}
							} else {
								logger.info(
										alert.getId() + " -> " + key + " Skip due to Source[" + sender + "] Unmatched");
							}
						} else {
							logger.info(alert.getId() + " -> " + key + " " + status + " count: " + count);
							if (status == null) {
								status = "QUEUED";
							}
							if (submitted_map.containsKey(key)) {
								submitted = (Integer) submitted_map.get(key);
							}
							submitted = submitted + count;
							submitted_map.put(key, submitted);
							if (alert.getStatus().equalsIgnoreCase(status)) {
								if (status_map.containsKey(key)) {
									statusCounter = (Integer) status_map.get(key);
								}
								statusCounter = statusCounter + count;
								status_map.put(key, statusCounter);
							}
						}
					}
				} catch (Exception ex) {
					logger.error(alert.getId() + "", ex);
				} finally {
					if (rs != null) {
						try {
							rs.close();
						} catch (Exception ex) {
						}
					}
					if (statement != null) {
						try {
							statement.close();
						} catch (Exception ex) {
						}
					}
					GlobalCache.connnection_pool_1.putConnection(connection);
				}
				// ************* preparing for results *************
				// logger.info("submitted_map: " + submitted_map);
				// logger.info("status_map: " + status_map);
				// Iterator itr = submitted_map.entrySet().iterator();
				for (Map.Entry<String, Integer> entry : submitted_map.entrySet()) {
					boolean isAlert = false;
					submitted = 0;
					statusCounter = 0;
					int percent = 0;
					String key = entry.getKey();
					submitted = entry.getValue();
					StringTokenizer token = new StringTokenizer(key, "#");
					String route = token.nextToken();
					String network_code = token.nextToken();
					String sender = null;
					if (token.hasMoreTokens()) {
						sender = token.nextToken();
					}
					String network = "";
					NetworkEntry networkEntry = GlobalVars.networkService
							.getNetworkEntry(Integer.parseInt(network_code));
					if (networkEntry != null) {
						network = networkEntry.getCountry() + "-" + networkEntry.getOperator();
					}
					logger.info(key + " Alert[" + alert.getId() + "] SubmittedCount: " + submitted + " minSubmitLimit:"
							+ alert.getMinlimit());
					// logger.info("Alert[" + alert.getId() + "] Submitted[" + key + "]:" + submitted);
					if (submitted > alert.getMinlimit()) {
						if (status_map.containsKey(key)) {
							statusCounter = (Integer) status_map.get(key);
						}
						logger.info(key + " Alert[" + alert.getId() + "] StatusCount[" + alert.getStatus() + "]: "
								+ statusCounter);
						percent = (int) (((double) statusCounter / (double) submitted) * 100);
						if (alert.getStatus().startsWith("DELIV")) {
							if (percent < alert.getPercent()) {
								isAlert = true;
							}
						} else {
							if (percent > alert.getPercent()) {
								isAlert = true;
								// send high non respond alert
							}
						}
						logger.info(
								key + " Alert[" + alert.getId() + "] Percent[" + alert.getStatus() + "]: " + percent);
						if (isAlert) {
							// logger.info("Alert[" + alert.getId() + "] Percent[" + key + "]:" + percent);
							// ******** Put result to database **************
							try {
								connection = GlobalCache.connnection_pool_1.getConnection();
								sql = "insert into perform_result(alert_id,start_time,end_time,percent,route,network,count,statusCount,sender) values(?,?,?,?,?,?,?,?,?)";
								statement = connection.prepareStatement(sql);
								statement.setInt(1, alert.getId());
								statement.setString(2, back_time);
								statement.setString(3, current_time);
								statement.setInt(4, percent);
								statement.setString(5, route);
								statement.setString(6, network_code);
								statement.setInt(7, submitted);
								statement.setInt(8, statusCounter);
								statement.setString(9, sender);
								statement.executeUpdate();
							} catch (Exception ex) {
								logger.error("", ex.fillInStackTrace());
							} finally {
								if (statement != null) {
									try {
										statement.close();
									} catch (Exception ex) {
									}
								}
								GlobalCache.connnection_pool_1.putConnection(connection);
							}
							if (alert.isHoldTraffic()) {
								// -------- Add to worst performance cache --------------
								if (alert.getStatus().startsWith("DELIV")) {
									Set<String> networks = null;
									if (GlobalCache.WorstDeliveryRoute.containsKey(route)) // Smsc Performance is worst
									{
										networks = GlobalCache.WorstDeliveryRoute.get(route);
									} else {
										networks = new HashSet<String>();
									}
									if (alert.isHoldTraffic()) {
										if (!networks.contains(network_code)) {
											networks.add(network_code);
											logger.info(alert.getId() + ": " + key + "-" + network
													+ " found as worst delivery performance");
										}
									} else {
										if (networks.contains(network_code)) {
											networks.remove(network_code);
											logger.info(alert.getId() + ": " + key + " " + network
													+ " Hold Traffic Disabled.");
										}
									}
									GlobalCache.WorstDeliveryRoute.put(route, networks);
								} else {
									Set<String> networks = null;
									if (GlobalCache.WorstResponseRoute.containsKey(route)) // Smsc Performance is worst
									{
										networks = GlobalCache.WorstResponseRoute.get(route);
									} else {
										networks = new HashSet<String>();
									}
									if (alert.isHoldTraffic()) {
										if (!networks.contains(network_code)) {
											networks.add(network_code);
											logger.info(alert.getId() + ": " + key + "-" + network
													+ " found as worst performance");
										}
									} else {
										if (networks.contains(network_code)) {
											networks.remove(network_code);
											logger.info(alert.getId() + ": " + key + " " + network
													+ " Hold Traffic Disabled.");
										}
									}
									GlobalCache.WorstResponseRoute.put(route, networks);
								}
								// ----------------- End --------------------------------
							} else {
								logger.info(alert.getId() + ": " + key + " " + network + " Hold Traffic Disabled.");
							}
							logger.info("Sending Alert[" + alert.getId() + "]: " + route + "[" + alert.getStatus() + "]"
									+ network);
							AlertDTO result = new AlertDTO(alert);
							result.setRoutes(route);
							result.setCountries(network);
							result.setFrom(back_time);
							result.setTo(current_time);
							result.setResultedPercent(percent);
							result.setSubmitted(submitted);
							result.setStatusCount(statusCounter);
							result.setSenders(sender);
							sendAlert(result);
						} else {
							if (alert.getStatus().startsWith("DELIV")) {
								if (GlobalCache.WorstDeliveryRoute.containsKey(route)) {
									Set<String> networks = GlobalCache.WorstDeliveryRoute.get(route);
									if (networks.contains(network_code)) {
										networks.remove(network_code);
										logger.info(route + "-" + network + " Removed as worst Delivery performance");
									}
									if (networks.isEmpty()) {
										GlobalCache.WorstDeliveryRoute.remove(route);
									}
								}
								// logger.info(route + " [" + alert.getStatus() + "]-> " + (Set) GlobalAppVars.WorstDeliveryRoute.get(route));
							} else {
								if (GlobalCache.WorstResponseRoute.containsKey(route)) {
									Set<String> networks = GlobalCache.WorstResponseRoute.get(route);
									if (networks.contains(network_code)) {
										networks.remove(network_code);
										logger.info(route + "-" + network + " Removed as worst performance");
									}
									if (networks.isEmpty()) {
										GlobalCache.WorstResponseRoute.remove(route);
									}
								}
								// logger.info(route + " [" + alert.getStatus() + "]-> " + (Set) GlobalAppVars.WorstResponseRoute.get(route));
							}
						}
					} else {
						logger.info(key + " Alert[" + alert.getId() + "] SubmittedCount(" + submitted
								+ ") less than minSubmitLimit(" + alert.getMinlimit() + ")");
						if (GlobalCache.WorstDeliveryRoute.containsKey(route)) {
							Set<String> networks = GlobalCache.WorstDeliveryRoute.get(route);
							if (networks.contains(network_code)) {
								networks.remove(network_code);
								logger.info(route + "-" + network + " Removed as worst Delivery performance");
							}
							if (networks.isEmpty()) {
								GlobalCache.WorstDeliveryRoute.remove(route);
							}
						}
						if (GlobalCache.WorstResponseRoute.containsKey(route)) {
							Set<String> networks = GlobalCache.WorstResponseRoute.get(route);
							if (networks.contains(network_code)) {
								networks.remove(network_code);
								logger.info(route + "-" + network + " Removed as worst performance");
							}
							if (networks.isEmpty()) {
								GlobalCache.WorstResponseRoute.remove(route);
							}
						}
					}
				}
			} else {
				logger.info("PerformanceAlert[" + alert.getId() + "] deactive");
			}
		}
		logger.info("PerformanceAlert[" + alert.getId() + "] Stopped");
	}

	private void sendAlert(AlertDTO result) {
		if (result.getEmail() != null) {
			sendEmailAlert(result);
		} else {
			logger.info("Invalid Email Found For Performance Alert: " + alert.getId());
		}
		if (test_user != null && test_password != null) {
			if (result.getAlertNumber() != null && result.getAlertNumber().length() > 0) {
				sendSMSAlert(result);
			} else {
				logger.info("Invalid Number Found For Performance SMS Alert: " + alert.getId());
			}
		} else {
			logger.info("No Internal User Found For Performance SMS Alert: " + alert.getId());
		}
	}

	private void sendEmailAlert(AlertDTO result) {
		String recipients[];
		StringTokenizer tokens = new StringTokenizer(result.getEmail(), ",");
		// recipients = new String[tokens.countTokens()];
		int i = 0;
		List<String> recipientlist = new ArrayList<String>();
		while (tokens.hasMoreTokens()) {
			String recipient = tokens.nextToken();
			if (recipient.contains("@") && recipient.contains(".")) {
				recipientlist.add(recipient);
				logger.info("<- Alert " + result.getId() + " Email: <" + recipient + "> ");
				i++;
			} else {
				logger.error("<- Alert " + result.getId() + " Invalid Email: <" + recipient + "> ");
			}
		}
		String routes = "-", countries = "-";
		if (result.getRoutes() != null) {
			routes = result.getRoutes();
		}
		if (result.getCountries() != null) {
			countries = result.getCountries();
		}
		recipients = recipientlist.toArray(new String[0]);
		String subject = "Performace Alert[" + result.getId() + "] On " + com.hti.util.Constants.SERVER_NAME;
		String mailMessage = "<body><span >Dear Team,</span><br><br>";
		mailMessage += "Please Find the Unexpected Result as below: <br><br>";
		mailMessage += "<table cellspacing='1' cellpadding='2' border='0' align='left'><tbody>";
		mailMessage += "<tr><td>Id</td><td>" + result.getId() + "</td></tr>";
		mailMessage += "<tr><td>Route(s)</td><td>" + routes + "</td></tr>";
		mailMessage += "<tr><td>Countries</td><td>" + countries + "</td></tr>";
		if (result.getSenders() != null) {
			mailMessage += "<tr><td>Sender</td><td>" + result.getSenders() + "</td></tr>";
		}
		mailMessage += "<tr><td>Duration</td><td>" + result.getDuration() + " Minutes</td></tr>";
		mailMessage += "<tr><td>From</td><td>" + result.getFrom() + "</td></tr>";
		mailMessage += "<tr><td>To</td><td>" + result.getTo() + "</td></tr>";
		mailMessage += "<tr><td>TotalSubmit</td><td>" + result.getSubmitted() + "</td></tr>";
		mailMessage += "<tr><td>" + result.getStatus() + "</td><td>" + result.getStatusCount() + "</td></tr>";
		mailMessage += "<tr><td>Expected(%)</td><td>" + result.getPercent() + "</td></tr>";
		mailMessage += "<tr><td>Resulted(%)</td><td>" + result.getResultedPercent() + "</td></tr>";
		mailMessage += "<tr><td>Remarks</td><td>" + result.getRemarks() + "</td></tr>";
		mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
		mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
		mailMessage += "<tr><td colspan='2'>Please Check and Do Needful.</td></tr>";
		mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
		mailMessage += "<tr><td colspan='2'>&nbsp;</td></tr>";
		mailMessage += "<tr><td colspan='2'>Thanks & Regards,</td></tr>";
		mailMessage += "<tr><td colspan='2'>Support Team - Broadnet</td></tr>";
		mailMessage += "</tbody></table><br><br><br><br>";
		mailMessage += "</body>";
		try {
			new GoogleTest().sendSSLMessage(recipients, subject, mailMessage);
		} catch (MessagingException me) {
			logger.error(me + " While Sending Performance Alert @ " + result.getEmail());
		}
	}

	private void sendSMSAlert(AlertDTO result) {
		com.logica.smpp.Connection conn = new TCPIPConnection(StartSmppServer.USER_SERVER_IP,
				StartSmppServer.USER_SERVER_PORT);
		Session session = new Session(conn, "local");
		BindRequest breq = new BindTransmitter();
		try {
			breq.setSystemId(test_user);
			breq.setPassword(test_password);
			logger.info("Sending Bind Request: " + test_user + " " + test_password);
			Response resp = session.bind(breq);
			logger.info(resp.debugString());
			if (resp.getCommandStatus() == Data.ESME_ROK) {
				String message = "Hello, On " + com.hti.util.Constants.SERVER_NAME + " Unexpected " + result.getStatus()
						+ "-" + result.getResultedPercent() + " AlertId-" + result.getId() + " Route-"
						+ result.getRoutes() + " Opr-" + result.getCountries();
				if (result.getSenders() != null) {
					message += " Sender-" + result.getSenders() + ".";
				} else {
					message += ".";
				}
				String destination = "";
				SubmitSM msg = null;
				StringTokenizer tokens = new StringTokenizer(result.getAlertNumber(), ",");
				while (tokens.hasMoreTokens()) {
					destination = tokens.nextToken();
					try {
						Long.parseLong(destination);
						msg = new SubmitSM();
						msg.setSourceAddr((byte) 5, (byte) 0, result.getStatus() + "-" + result.getId());
						msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
						msg.setRegisteredDelivery((byte) 1);
						msg.setDataCoding((byte) 0);
						msg.setEsmClass((byte) 0);
						msg.setShortMessage(message);
						resp = session.submit(msg);
						if (resp.getCommandStatus() == Data.ESME_ROK) {
							logger.info(result.getId() + " Alert Message submitted -> " + destination);
						} else {
							logger.error(result.getId() + " Alert Message submission failed. Status="
									+ resp.getCommandStatus() + " -> " + destination);
						}
					} catch (NumberFormatException ne) {
						logger.error("Invalid Alert Number Found For Alert " + result.getId() + ": " + destination);
					}
				}
				UnbindResp unbind = session.unbind();
				if (unbind != null) {
					logger.info(test_user + " -> " + unbind.debugString());
				}
			} else {
				logger.error(test_user + " SMPP Connection Error: " + resp.getCommandStatus());
			}
		} catch (Exception ex) {
			logger.error("sendSMSAlert(" + result.getId() + ")", ex);
		}
	}

	private void getInternalUser() {
		UserEntry userEntry = GlobalVars.userService.getInternalUser();
		if (userEntry != null) {
			test_user = userEntry.getSystemId();
			test_password = userEntry.getPassword();
		}
	}

	public void stop() {
		logger.info("PerformanceAlert[" + alert.getId() + "] Stopping");
		stop = true;
		if (sleep) {
			logger.info("PerformanceAlert[" + alert.getId() + "] Interrupting");
			thread.interrupt();
		}
	}
}
