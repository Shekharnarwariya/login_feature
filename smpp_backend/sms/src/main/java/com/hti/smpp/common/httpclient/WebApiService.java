/* 
 **	Copyright 2004 High Tech InfoSystems. All Rights Reserved.
 **	Author		: Satya Prakash [satyaprakash@utils.net]
 **	Created on 	: 12/06/2004
 **	Modified on	: 12/06/2004
 **	Descritpion	: 
 */
package com.hti.smpp.common.httpclient;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.ProfessionEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.FlagStatus;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MultiUtility;

@Service
public class WebApiService {
	private Logger logger = LoggerFactory.getLogger(WebApiService.class);
	@Autowired
	private IDatabaseService dbService;

	public String getUrlUserBalance(String systemId, String password) throws Exception {
		UserService userService = new UserService();
		int userid = userService.validateUser(systemId, password);
		if (userid > 0) {
			BalanceEntry entry = GlobalVars.BalanceEntries.get(userid);
			if (entry.getWalletFlag().equalsIgnoreCase("No")) {
				return String.valueOf(entry.getCredits());
			} else {
				return String.valueOf(entry.getWalletAmount());
			}
		}
		return "INVALID USER";
	}

	public String getUrlUserBalance(String accessKey) throws Exception {
		UserService userService = new UserService();
		int userid = userService.validateUser(accessKey);
		if (userid > 0) {
			BalanceEntry entry = GlobalVars.BalanceEntries.get(userid);
			if (entry.getWalletFlag().equalsIgnoreCase("No")) {
				return String.valueOf(entry.getCredits());
			} else {
				return String.valueOf(entry.getWalletAmount());
			}
		}
		return "INVALID USER";
	}

	public JSONObject checkBalance(String targetUser) {
		JSONObject resp = new JSONObject();
		if (GlobalVars.UserMapping.containsKey(targetUser)) {
			int userid = GlobalVars.UserMapping.get(targetUser);
			BalanceEntry entry = GlobalVars.BalanceEntries.get(userid);
			if (entry.getWalletFlag().equalsIgnoreCase("No")) {
				resp.put("amount", String.valueOf(entry.getCredits()));
				resp.put("balance_mode", "credit");
			} else {
				resp.put("amount", String.valueOf(entry.getWalletAmount()));
				resp.put("balance_mode", "wallet");
			}
			logger.info(targetUser + " " + resp.toString());
		} else {
			resp.put("amount", "0");
			resp.put("balance_mode", "");
			logger.error(targetUser + " < No Record Found >");
		}
		return resp;
	}

	public String recharge(WebRechargeEntry entry) {
		logger.info("Processing For Recharge: " + entry);
		String request_user = entry.getMaster();
		String request_user_role = entry.getRole();
		String operation = entry.getOperation();
		String recharge_type = "Local";
		String remarks = "API Recharge";
		long toBeAddedCredits = 0;
		double toBeAddedBalance = 0;
		int target_user_id = GlobalVars.UserMapping.get(entry.getTargetUser());
		UserEntry targetUserEntry = GlobalVars.UserEntries.get(target_user_id);
		int master_user_id = GlobalVars.UserMapping.get(targetUserEntry.getMasterId());
		UserEntry masterUserEntry = GlobalVars.UserEntries.get(master_user_id);
		BalanceEntry master_balance = GlobalVars.BalanceEntries.get(master_user_id);
		if (request_user_role.equalsIgnoreCase("admin")) {
			if (master_balance.getWalletFlag().equalsIgnoreCase("No")) {
				entry.setBalanceMode("credit");
			} else {
				entry.setBalanceMode("wallet");
			}
		}
		if (entry.getBalanceMode().equalsIgnoreCase("credit")) {
			toBeAddedCredits = Long.parseLong(entry.getAmount());
		} else {
			toBeAddedBalance = Double.parseDouble(entry.getAmount());
		}
		RechargeEntry rechargeEntryUser = null;
		if (request_user_role.equalsIgnoreCase("superadmin")
				&& entry.getTargetUser().equalsIgnoreCase(entry.getMaster())) {// own account recharge by superadmin
			try {
				logger.info("Superadmin Recharging his Own Account: Wallet: " + toBeAddedBalance + " Credits: "
						+ toBeAddedCredits);
				double effectiveBalance = master_balance.getWalletAmount();
				long effectiveCredits = master_balance.getCredits();
				double preBalance = master_balance.getWalletAmount();
				long preCredits = master_balance.getCredits();
				boolean isProcessed = true;
				if (entry.getBalanceMode().equalsIgnoreCase("wallet")) {
					isProcessed = addBalance(masterUserEntry.getId(), toBeAddedBalance);
					effectiveBalance = effectiveBalance + toBeAddedBalance;
					logger.info(
							masterUserEntry.getSystemId() + " Balance Added. Effective Balance: " + effectiveBalance);
				} else {
					isProcessed = addCredits(masterUserEntry.getId(), (int) toBeAddedCredits);
					effectiveCredits = effectiveCredits + toBeAddedCredits;
					logger.info(
							masterUserEntry.getSystemId() + " Credits Added. Effective Credits: " + effectiveCredits);
				}
				if (isProcessed) {
					logger.info(masterUserEntry.getSystemId() + " isProcessed: " + isProcessed);
					// ------------------ First Entry For Admin --------------------
					String particular = null;
					if (operation.equalsIgnoreCase("plus")) {
						particular = "cr_" + masterUserEntry.getSystemId();
					} else {
						particular = "dr_" + masterUserEntry.getSystemId();
					}
					rechargeEntryUser = new RechargeEntry(masterUserEntry.getId(),
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), particular, preCredits,
							toBeAddedCredits, preBalance, toBeAddedBalance, remarks, recharge_type,
							masterUserEntry.getSystemId());
					rechargeEntryUser.setEffectiveCredits(effectiveCredits);
					rechargeEntryUser.setEffectiveWallet(effectiveBalance);
					logger.info(masterUserEntry.getSystemId() + " : " + rechargeEntryUser);
					UserService userService = new UserService();
					userService.saveRechargeEntry(rechargeEntryUser);
				} else {
					logger.info(masterUserEntry.getSystemId() + " Balance Processing Error");
					return ResponseCode.SYSTEM_ERROR;
				}
			} catch (Exception e) {
				logger.info(masterUserEntry.getSystemId(), e.fillInStackTrace());
				return ResponseCode.SYSTEM_ERROR;
			}
		} else {
			try {
				logger.info(request_user + " Balance " + operation + " Request From " + masterUserEntry.getSystemId()
						+ " To: " + targetUserEntry.getSystemId() + " Wallet: " + toBeAddedBalance + " Credit: "
						+ toBeAddedCredits);
				String user_particular = null;
				if (operation.equalsIgnoreCase("plus")) {
					user_particular = "cr_" + masterUserEntry.getSystemId();
				} else {
					user_particular = "dr_" + masterUserEntry.getSystemId();
				}
				String admin_particular = null;
				if (operation.equalsIgnoreCase("plus")) {
					admin_particular = "dr_" + targetUserEntry.getSystemId();
				} else {
					admin_particular = "cr_" + targetUserEntry.getSystemId();
				}
				BalanceEntry target_balance = GlobalVars.BalanceEntries.get(target_user_id);
				rechargeEntryUser = new RechargeEntry(targetUserEntry.getId(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), user_particular,
						target_balance.getCredits(), toBeAddedCredits, target_balance.getWalletAmount(),
						toBeAddedBalance, remarks, recharge_type, request_user);
				RechargeEntry rechargeEntryAdmin = new RechargeEntry(masterUserEntry.getId(),
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), admin_particular,
						master_balance.getCredits(), toBeAddedCredits, master_balance.getWalletAmount(),
						toBeAddedBalance, remarks, recharge_type, request_user);
				// ---------------------------------------------------------------
				double adminWallet = master_balance.getWalletAmount();
				double userWallet = target_balance.getWalletAmount();
				long adminCredits = master_balance.getCredits();
				long userCredits = target_balance.getCredits();
				if (entry.getBalanceMode().equalsIgnoreCase("wallet")) {
					if (operation.equalsIgnoreCase("plus")) {
						if (targetUserEntry.isAdminDepend()) { // inherited user
							double allocatedBalance = 0;
							Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
									.equal(masterUserEntry.getSystemId());
							Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
							logger.info(masterUserEntry.getSystemId() + " Depend Users: " + users.size());
							for (UserEntry child : users) {
								BalanceEntry child_balance = GlobalVars.BalanceEntries.get(child.getId());
								allocatedBalance += child_balance.getWalletAmount();
							}
							logger.info(masterUserEntry.getSystemId() + "[" + adminWallet
									+ "] Previous Allocated Balance: " + allocatedBalance);
							if ((toBeAddedBalance + allocatedBalance) <= (adminWallet * 2)) {
								logger.info("Adding " + toBeAddedBalance + " From: " + masterUserEntry.getSystemId()
										+ " To Child: " + targetUserEntry.getSystemId() + " [" + userWallet + "]");
								boolean isProcessed = addBalance(targetUserEntry.getId(), toBeAddedBalance);
								if (isProcessed) {
									logger.info(masterUserEntry.getSystemId() + " <Admin> has Sufficient Amount");
									userWallet += toBeAddedBalance;
								} else {
									logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Amount");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Amount: "
										+ adminWallet);
								return ResponseCode.INSUF_BALANCE;
							}
						} else {
							if (adminWallet > toBeAddedBalance) {
								logger.info("Transfer " + toBeAddedBalance + " From: " + masterUserEntry.getSystemId()
										+ "[" + adminWallet + "] To: " + targetUserEntry.getSystemId() + "["
										+ userWallet + "]");
								boolean isProcessed = transferBalance(masterUserEntry.getId(), targetUserEntry.getId(),
										toBeAddedBalance);
								if (isProcessed) {
									userWallet += toBeAddedBalance;
									adminWallet -= toBeAddedBalance;
								} else {
									logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Amount");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Amount: "
										+ adminWallet);
								return ResponseCode.INSUF_BALANCE;
							}
						}
					} else {
						if (targetUserEntry.isAdminDepend()) { // inherited user
							logger.info("Deduction " + toBeAddedBalance + " From: " + masterUserEntry.getSystemId()
									+ "[" + adminWallet + "] To Child : " + targetUserEntry.getSystemId() + "["
									+ userWallet + "]");
							if ((userWallet - toBeAddedBalance) >= 0) {
								boolean isProcessed = deductBalance(targetUserEntry.getId(), toBeAddedBalance);
								if (isProcessed) {
									userWallet -= toBeAddedBalance;
								} else {
									logger.info(
											targetUserEntry.getSystemId() + "<User> has Insufficient Amount to Deduct");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(targetUserEntry.getSystemId() + "<User> has Insufficient Amount to Deduct");
								return ResponseCode.INSUF_BALANCE;
							}
						} else {
							logger.info("Deduction " + toBeAddedBalance + " From: " + masterUserEntry.getSystemId()
									+ "[" + adminWallet + "] To : " + targetUserEntry.getSystemId() + "[" + userWallet
									+ "]");
							if ((userWallet - toBeAddedBalance) >= 0) {
								boolean isProcessed = transferBalance(targetUserEntry.getId(), masterUserEntry.getId(),
										toBeAddedBalance);
								if (isProcessed) {
									userWallet -= toBeAddedBalance;
									adminWallet += toBeAddedBalance;
								} else {
									logger.info(
											targetUserEntry.getSystemId() + "<User> has Insufficient Amount to Deduct");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(targetUserEntry.getSystemId() + "<User> has Insufficient Amount to Deduct");
								return ResponseCode.INSUF_BALANCE;
							}
						}
					}
				} else {
					if (operation.equalsIgnoreCase("plus")) {
						if (targetUserEntry.isAdminDepend()) { // inherited user
							long allocatedCredits = 0;
							Predicate p = new PredicateBuilderImpl().getEntryObject().get("masterId")
									.equal(masterUserEntry.getSystemId());
							Collection<UserEntry> users = GlobalVars.UserEntries.values(p);
							logger.info(masterUserEntry.getSystemId() + " Depend Users: " + users.size());
							for (UserEntry child : users) {
								BalanceEntry child_balance = GlobalVars.BalanceEntries.get(child.getId());
								allocatedCredits += child_balance.getCredits();
							}
							logger.info(masterUserEntry.getSystemId() + "[" + adminCredits
									+ "] Previous Allocated Credits: " + allocatedCredits);
							if (((toBeAddedCredits + allocatedCredits) <= (adminCredits * 2))) {
								logger.info("Credit Addition " + toBeAddedCredits + "  From: "
										+ masterUserEntry.getSystemId() + "[" + adminCredits + "] To Child: "
										+ targetUserEntry.getSystemId() + "[" + userCredits + "]");
								boolean isProcessed = addCredits(targetUserEntry.getId(), (int) toBeAddedCredits);
								if (isProcessed) { // Processing from smpp server
									userCredits += toBeAddedCredits;
								} else {
									logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Credit");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Credit");
								return ResponseCode.INSUF_BALANCE;
							}
						} else {
							logger.info("Credits " + toBeAddedCredits + " Transfer From: "
									+ masterUserEntry.getSystemId() + "[" + adminCredits + "] To: "
									+ targetUserEntry.getSystemId() + "[" + userCredits + "]");
							if ((adminCredits > toBeAddedCredits)) {
								boolean isProcessed = transferCredits(masterUserEntry.getId(), targetUserEntry.getId(),
										(int) toBeAddedCredits);
								if (isProcessed) { // Processing from smpp server
									userCredits += toBeAddedCredits;
									adminCredits -= toBeAddedCredits;
								} else {
									logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Credit");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(masterUserEntry.getSystemId() + " <Admin> has Insufficient Credit");
								return ResponseCode.INSUF_BALANCE;
							}
						}
					} else {
						if (targetUserEntry.isAdminDepend()) { // inherited user
							logger.info("Credits " + toBeAddedCredits + " Deduction From: "
									+ masterUserEntry.getSystemId() + "[" + adminCredits + "] To Child: "
									+ targetUserEntry.getSystemId() + "[" + userCredits + "]");
							if ((userCredits - toBeAddedCredits) >= 0) {
								boolean isProcessed = deductCredits(targetUserEntry.getId(), (int) toBeAddedCredits);
								if (isProcessed) { // Processing from smpp server
									userCredits -= toBeAddedCredits;
								} else {
									logger.info(targetUserEntry.getSystemId()
											+ " <User> has Insufficient Credit to deduct");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(
										targetUserEntry.getSystemId() + " <User> has Insufficient Credit to deduct");
								return ResponseCode.INSUF_BALANCE;
							}
						} else {
							logger.info("Credits " + toBeAddedCredits + " Deduction From: "
									+ masterUserEntry.getSystemId() + "[" + adminCredits + "] To: "
									+ targetUserEntry.getSystemId() + "[" + userCredits + "]");
							if ((userCredits - toBeAddedCredits) >= 0) {
								boolean isProcessed = transferCredits(targetUserEntry.getId(), masterUserEntry.getId(),
										(int) toBeAddedCredits);
								if (isProcessed) { // Processing from smpp server
									userCredits -= toBeAddedCredits;
									adminCredits += toBeAddedCredits;
								} else {
									logger.info(targetUserEntry.getSystemId()
											+ " <User> has Insufficient Credit to deduct");
									return ResponseCode.INSUF_BALANCE;
								}
							} else {
								logger.info(
										targetUserEntry.getSystemId() + " <User> has Insufficient Credit to deduct");
								return ResponseCode.INSUF_BALANCE;
							}
						}
					}
				}
				logger.info(request_user + " Saving Recharge Entries");
				// ----------- save Recharge Entry -------------------------------
				UserService userService = new UserService();
				rechargeEntryAdmin.setEffectiveWallet(adminWallet);
				rechargeEntryAdmin.setEffectiveCredits(adminCredits);
				userService.saveRechargeEntry(rechargeEntryAdmin);
				rechargeEntryUser.setEffectiveWallet(userWallet);
				rechargeEntryUser.setEffectiveCredits(userCredits);
				userService.saveRechargeEntry(rechargeEntryUser);
			} catch (Exception e) {
				logger.info(request_user, e.fillInStackTrace());
				return ResponseCode.SYSTEM_ERROR;
			}
		}
		// ----------- send invoice email --------------------
		logger.info(request_user + " Preparing Invoice Email For: " + targetUserEntry.getSystemId());
		try {
			String flagVal = GlobalVars.UserFlagStatus.get(targetUserEntry.getSystemId());
			if ((flagVal != null) && (!flagVal.equalsIgnoreCase(FlagStatus.BLOCKED))) {
				MultiUtility.changeFlag(Constants.USER_FLAG_DIR + targetUserEntry.getSystemId() + ".txt",
						FlagStatus.BALANCE_REFRESH);
			}
			if (!targetUserEntry.getSystemId().equalsIgnoreCase(targetUserEntry.getMasterId())) {
				String masterflagVal = GlobalVars.UserFlagStatus.get(targetUserEntry.getMasterId());
				if (masterflagVal != null && !masterflagVal.equalsIgnoreCase(FlagStatus.BLOCKED)) {
					MultiUtility.changeFlag(Constants.USER_FLAG_DIR + targetUserEntry.getMasterId() + ".txt",
							FlagStatus.BALANCE_REFRESH); // reload user account details
				}
			}
			MultiUtility.changeFlag(Constants.CLIENT_FLAG_FILE, FlagStatus.REFRESH); // command to reload users flag
																						// files
			String email = "";
			WebMasterEntry web_entry = GlobalVars.WebmasterEntries.get(target_user_id);
			if (request_user_role.equalsIgnoreCase("superadmin") || request_user_role.equalsIgnoreCase("system")) {
				for (String finance_email : IConstants.FINANCE_EMAIL) {
					email += finance_email + ",";
				}
				if (email.length() > 0) {
					email = email.substring(0, email.length() - 1);
				}
				if (web_entry.getInvoiceEmail() != null && web_entry.getInvoiceEmail().contains(".")
						&& web_entry.getInvoiceEmail().contains("@")) {
					email += "," + web_entry.getInvoiceEmail();
				} else {
					logger.info(targetUserEntry.getSystemId() + "Invoice Email Not Found");
				}
			} else {
				if (web_entry.getInvoiceEmail() != null) {
					email = web_entry.getInvoiceEmail();
				} else {
					logger.info(targetUserEntry.getSystemId() + "Invoice Email Not Found");
				}
			}
			if (!email.contains("@") && !email.contains(".")) {
				email = "";
				for (String finance_email : IConstants.FINANCE_EMAIL) {
					email += finance_email + ",";
				}
			}
			String htmlString = "";
			String preBal = "-", amount = "-", effBal = "-";
			if (operation.equalsIgnoreCase("Credit")) {
				preBal = String.valueOf(rechargeEntryUser.getPreviousCredits());
				amount = String.valueOf(rechargeEntryUser.getToBeAddedCedits());
				effBal = String.valueOf(rechargeEntryUser.getEffectiveCredits());
			} else {
				preBal = String.valueOf(rechargeEntryUser.getPreviousWallet());
				amount = String.valueOf(rechargeEntryUser.getToBeAddedWallet());
				effBal = String.valueOf(rechargeEntryUser.getEffectiveWallet());
			}
			htmlString += "<body>" + "<span >Dear User,</span>" + "<br>" + "<br>" + "<span >System ID   : <strong >"
					+ targetUserEntry.getSystemId() + "</strong></span><br />" + "<br>"
					+ "<span >Please Find Recharge Details as Below :" + "</span><br />" + "<br>" + "<br>"
					+ "<table width='758' height='48' cellspacing='1' cellpadding='2' border='0' align='center' summary=''>"
					+ "<tbody>" + "<tr align='center'>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Username</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Usermode</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Previous Balance</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Recharge Amount</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Effective Balance</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Time</strong></font></td>"
					+ "<td bgcolor='#A8A8A8'><font size='2'><strong>Remarks</strong></font></td>" + "</tr>"
					+ "<tr align='center'>" + "<td>" + targetUserEntry.getSystemId() + "</td>" + "<td>" + operation
					+ "</td>" + "<td>" + preBal + "</td>" + "<td>" + amount + "</td>" + "<td>" + effBal + "</td>"
					+ "<td>" + rechargeEntryUser.getTime() + "</td>" + "<td>" + remarks + "</td>" + "</tr>" + "</tbody>"
					+ "</table>" + "<br><br><br><br><br><br>" + "<strong><span >Kind Regards,</span><br>"
					+ "<span >Support Team</span></strong><br>" + "<br>" + "</body>";
			String reporthtml = IConstants.WEBSMPP_EXT_DIR + "mail//" + targetUserEntry.getSystemId() + "_recharge_"
					+ new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date()) + ".html";
			MultiUtility.writeMailContent(reporthtml, htmlString);
			String subject = "Recharge : " + targetUserEntry.getSystemId() + "_"
					+ new SimpleDateFormat("dd-MMM-yyyy_HHmmss").format(new Date());
			String from = IConstants.SUPPORT_EMAIL[0];
			UserService userService = new UserService();
			ProfessionEntry professionEntry = userService.getProfessionEntry(targetUserEntry.getSystemId());
			if (professionEntry != null && professionEntry.getDomainEmail() != null
					&& professionEntry.getDomainEmail().length() > 0 && professionEntry.getDomainEmail().contains("@")
					&& professionEntry.getDomainEmail().contains(".")) {
				from = professionEntry.getDomainEmail();
				logger.info(targetUserEntry.getSystemId() + " Domain-Email Found: " + from);
			} else {
				String master = targetUserEntry.getMasterId();
				ProfessionEntry masterProfessionEntry = userService.getProfessionEntry(master);
				if (masterProfessionEntry != null && masterProfessionEntry.getDomainEmail() != null
						&& masterProfessionEntry.getDomainEmail().length() > 0
						&& masterProfessionEntry.getDomainEmail().contains("@")
						&& masterProfessionEntry.getDomainEmail().contains(".")) {
					from = masterProfessionEntry.getDomainEmail();
					logger.info(targetUserEntry.getSystemId() + " Master Domain-Email Found: " + from);
				} else {
					logger.info(targetUserEntry.getSystemId() + " Domain-Email Not Found");
				}
			}
			MailUtility.send(email, reporthtml, subject, from, false);
			logger.info("Recharge Email Sent From:" + from + " To:" + email);
		} catch (Exception e) {
			logger.error(request_user_role + " Invoice Email Error", e.getMessage());
		}
		return ResponseCode.NO_ERROR;
	}

	// ----------------- private balance methods -----------------------
	private boolean addCredits(int userId, long amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			balanceEntry.setCredits(balanceEntry.getCredits() + amount);
			balanceEntry.setActive(true);
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}

	private boolean addBalance(int userId, double amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() + amount);
			if (balanceEntry.getWalletFlag().equalsIgnoreCase("MIN")) {
				balanceEntry.setWalletFlag("Yes");
			}
			balanceEntry.setActive(true);
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}

	public boolean transferBalance(int from, int to, double amount) throws EntryNotFoundException {
		logger.info("Transfer From: " + from + " To: " + to + " Credits:" + amount);
		if (!GlobalVars.BalanceEntries.containsKey(to)) {
			throw new EntryNotFoundException(to + " BalanceEntry");
		}
		if (!GlobalVars.BalanceEntries.containsKey(from)) {
			throw new EntryNotFoundException(from + " BalanceEntry");
		}
		boolean proceed = false;
		if (GlobalVars.BalanceEntries.containsKey(to)) {
			try {
				GlobalVars.BalanceEntries.lock(from);
				BalanceEntry from_balance = GlobalVars.BalanceEntries.get(from);
				if (from_balance.getWalletAmount() > amount) {
					from_balance.setWalletAmount(from_balance.getWalletAmount() - amount);
					if (from_balance.getWalletFlag().equalsIgnoreCase("MIN")) {
						from_balance.setWalletFlag("Yes");
					}
					logger.info(from + " Remaining Balance -->" + from_balance.getWalletAmount());
					from_balance.setActive(true);
					proceed = true;
				} else {
					logger.error(from + " Insufficient Balance -->" + from_balance.getWalletAmount());
				}
				GlobalVars.BalanceEntries.put(from, from_balance);
			} finally {
				GlobalVars.BalanceEntries.unlock(from);
			}
			if (proceed) {
				try {
					GlobalVars.BalanceEntries.lock(to);
					BalanceEntry to_balance = GlobalVars.BalanceEntries.get(to);
					to_balance.setWalletAmount(to_balance.getWalletAmount() + amount);
					if (to_balance.getWalletFlag().equalsIgnoreCase("MIN")) {
						to_balance.setWalletFlag("Yes");
					}
					to_balance.setActive(true);
					GlobalVars.BalanceEntries.put(to, to_balance);
					logger.info(to + " Effective Balance -->" + to_balance.getCredits());
				} finally {
					GlobalVars.BalanceEntries.unlock(to);
				}
			}
		} else {
			logger.error(to + " < Balance Entry Not Found >");
		}
		return proceed;
	}

	public boolean transferCredits(int from, int to, int amount) throws EntryNotFoundException {
		logger.info("Transfer From: " + from + " To: " + to + " Credits:" + amount);
		if (!GlobalVars.BalanceEntries.containsKey(to)) {
			throw new EntryNotFoundException(to + " BalanceEntry");
		}
		if (!GlobalVars.BalanceEntries.containsKey(from)) {
			throw new EntryNotFoundException(from + " BalanceEntry");
		}
		boolean proceed = false;
		if (GlobalVars.BalanceEntries.containsKey(to)) {
			try {
				GlobalVars.BalanceEntries.lock(from);
				BalanceEntry from_balance = GlobalVars.BalanceEntries.get(from);
				if (from_balance.getCredits() > amount) {
					from_balance.setCredits(from_balance.getCredits() - amount);
					logger.info(from + " Remaining Credits -->" + from_balance.getCredits());
					from_balance.setActive(true);
					proceed = true;
				} else {
					logger.error(from + " Insufficient Credits -->" + from_balance.getCredits());
				}
				GlobalVars.BalanceEntries.put(from, from_balance);
			} finally {
				GlobalVars.BalanceEntries.unlock(from);
			}
			if (proceed) {
				try {
					GlobalVars.BalanceEntries.lock(to);
					BalanceEntry to_balance = GlobalVars.BalanceEntries.get(to);
					to_balance.setCredits(to_balance.getCredits() + amount);
					to_balance.setActive(true);
					GlobalVars.BalanceEntries.put(to, to_balance);
					logger.info(to + " Effective Credits -->" + to_balance.getCredits());
				} finally {
					GlobalVars.BalanceEntries.unlock(to);
				}
			}
		} else {
			logger.error(to + " < Balance Entry Not Found >");
		}
		return proceed;
	}

	private boolean deductCredits(int userId, int amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			if (balanceEntry.getCredits() >= amount) {
				balanceEntry.setCredits(balanceEntry.getCredits() - amount);
				balanceEntry.setActive(true);
				logger.info(userId + " Effective Credits -->" + balanceEntry.getCredits());
			} else {
				logger.error(userId + " Insufficient Credits -->" + balanceEntry.getCredits());
			}
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}

	private boolean deductBalance(int userId, double amount) throws EntryNotFoundException {
		if (!GlobalVars.BalanceEntries.containsKey(userId)) {
			throw new EntryNotFoundException(userId + " BalanceEntry");
		}
		try {
			GlobalVars.BalanceEntries.lock(userId);
			BalanceEntry balanceEntry = GlobalVars.BalanceEntries.get(userId);
			if (balanceEntry.getWalletAmount() >= amount) {
				balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() - amount);
				if (balanceEntry.getWalletFlag().equalsIgnoreCase("MIN")) {
					balanceEntry.setWalletFlag("Yes");
				}
				balanceEntry.setActive(true);
				logger.info(userId + " Effective Balance -->" + balanceEntry.getWalletAmount());
			} else {
				logger.error(userId + " Insufficient Balance -->" + balanceEntry.getWalletAmount());
			}
			GlobalVars.BalanceEntries.put(userId, balanceEntry);
		} finally {
			GlobalVars.BalanceEntries.unlock(userId);
		}
		return true;
	}
}
