package com.hti.smpp.common.httpclient;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.session.UserSession;
import com.hti.smpp.common.util.GlobalVarsSms;
import com.hti.smpp.common.util.IConstants;
import com.logica.smpp.Data;
import com.logica.smpp.Session;
import com.logica.smpp.TimeoutException;
import com.logica.smpp.WrongSessionStateException;
import com.logica.smpp.pdu.PDUException;
import com.logica.smpp.pdu.SubmitSM;
import com.logica.smpp.pdu.WrongLengthOfStringException;

public class HlrClient {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private int commandid = 0;
	private Session session = null;

	public HlrClient() {
	}

	private static synchronized UserSession getHlrUserSession(String user, String pwd) {
		UserSession userSession = null;
		if (GlobalVarsSms.HlrUserSessionHandler.containsKey(user + "#" + pwd)) {
			userSession = ((HlrSessionHandler) GlobalVarsSms.HlrUserSessionHandler.get(user + "#" + pwd))
					.getUserSession();
		} else {
			userSession = new HlrSessionHandler(user, pwd).getUserSession();
		}
		return userSession;
	}

	public String processHlr(LookupObject lookupObject) {
		String ret = null;
		String user = lookupObject.getSystemid();
		String pwd = lookupObject.getPassword();
		UserSession userSession = getHlrUserSession(user, pwd);
		commandid = userSession.getCommandStatus();
		if (commandid != Data.ESME_ROK) {
			logger.error(user + " Connection Error: " + commandid);
			if (commandid == Data.ESME_RINVSYSID) {
				ret = IConstants.ERROR_HTTP05;
				// System.out.println("<- " + user + " (HTTP) Connection Error. < Invalid System
				// ID >");
			} else if (commandid == Data.ESME_RINVPASWD) {
				ret = IConstants.ERROR_HTTP04;
				// System.out.println("<- " + user + " (HTTP) Connection Error. < Invalid System
				// ID/Password >");
			} else if (commandid == 1035) {
				ret = IConstants.ERROR_HTTP18;
				// System.out.println("<- " + user + " (HTTP) Connection Error. < Insufficient
				// Balance >");
			} else {
				ret = IConstants.ERROR_HTTP14;
				// System.out.println("<- " + user + " (HTTP) Connection Error. Commandid: " +
				// commandid);
			}
		} else {
			session = userSession.getSession();
			String batchid = lookupObject.getBatchid();
			SubmitSM msg = null;
			List<String> list = lookupObject.getList();
			logger.info(user + "[" + batchid + "] Hlr Requested Numbers:-> " + list.size());
			int processed = 0;
			while (!list.isEmpty()) {
				String destination = (String) list.remove(0);
				msg = new SubmitSM();
				try {
					msg.setSourceAddr((byte) 5, (byte) 0, "HLR");
					msg.setDestAddr(Data.GSM_TON_INTERNATIONAL, Data.GSM_NPI_E164, destination);
					msg.setShortMessage(batchid);
					msg.setRegisteredDelivery((byte) 1);
					msg.setDataCoding((byte) 0);
				} catch (WrongLengthOfStringException ex) {
					logger.error(user + "[" + batchid + "] -> (HLR)SubmitSM Creation Error " + ex);
				}
				try {
					session.submit(msg);
					processed++;
				} catch (TimeoutException | PDUException | WrongSessionStateException ex) {
					logger.error(user + "[" + batchid + "] onSubmit(HLR): " + ex);
				} catch (Exception ex) {
					ret = IConstants.ERROR_HTTP12;
					logger.error(user + "[" + batchid + "] <- (HLR)Session Disconnected -> ");
					break;
				}
			}
			logger.info(user + "[" + batchid + "] Hlr Processed Numbers:-> " + processed);
		}
		return ret;
	}
}
