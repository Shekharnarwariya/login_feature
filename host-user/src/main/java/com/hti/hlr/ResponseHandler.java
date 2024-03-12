package com.hti.hlr;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.exception.EntryNotFoundException;
import com.hti.objects.RoutingDTO;
import com.hti.objects.SmscInObj;
import com.hti.user.UserBalance;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalQueue;
import com.hti.util.GlobalVars;
import com.logica.smpp.Data;
import com.logica.smpp.util.Queue;

public class ResponseHandler implements Runnable {
	private Logger logger = LoggerFactory.getLogger("hlrLogger");
	private boolean stop;
	private String systemId;
	private Map<Integer, RoutingDTO> RoutingMap = null;
	private Map<String, Integer> NetworkMap = null;
	private Queue responeQueue;
	private String threadId;
	private boolean recordMnp;

	public ResponseHandler(String systemId, String threadId) {
		this.systemId = systemId;
		this.threadId = threadId;
		this.responeQueue = GlobalVar.HlrResponeQueue.get(systemId);
		this.recordMnp = GlobalVars.userService.getUserEntry(systemId).isRecordMnp();
		logger.info(systemId + "_" + threadId + " ResponseHandler Starting");
	}

	public void refresh() {
		RoutingMap = null;
		NetworkMap = null;
	}

	@Override
	public void run() {
		while (!stop) {
			if (!responeQueue.isEmpty()) {
				HlrRequest hlrRequest = null;
				LookupDTO lookupDTO = null;
				while (!responeQueue.isEmpty()) {
					hlrRequest = (HlrRequest) responeQueue.dequeue();
					logger.debug(systemId + ": " + hlrRequest);
					if (hlrRequest.getResponse().getCommandId() == Data.SUBMIT_SM_RESP) {
						RouteObject routeObject = null;
						if (GlobalVar.EnqueueRouteObject.containsKey(hlrRequest.getMessageId())) {
							routeObject = GlobalVar.EnqueueRouteObject.get(hlrRequest.getMessageId());
							if (hlrRequest.getResponse().getCommandStatus() == Data.ESME_ROK) {
								lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "S", "ATES", LookupStatus.NO_ERROR,
										"NO_ERROR", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										hlrRequest.getResponse().getResponseId(), "resp");
								GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
							} else { // no dlr will be received. process related pdu
								lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "E", "FAILED",
										hlrRequest.getResponse().getCommandStatus() + "", "SUBMIT_ERROR",
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 0 + "", "resp");
								GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
								logger.info(hlrRequest.getMessageId() + " No Dlr Will be Received.proceed further");
								GlobalVar.EnqueueRouteObject.remove(hlrRequest.getMessageId());
								GlobalQueue.smsc_in_temp_update_Queue.enqueue(new SmscInObj(routeObject.getMsgId(), "C",
										routeObject.getSmsc(), routeObject.getGroupId(), routeObject.getCost(),
										routeObject.getNetworkId()));
							}
						} else {
							logger.error(hlrRequest.getMessageId() + " RouteObject Not Found[Resp].");
							if (hlrRequest.getResponse().getCommandStatus() == Data.ESME_ROK) {
								lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "S", "ATES", LookupStatus.NO_ERROR,
										"NO_ERROR", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
										hlrRequest.getResponse().getResponseId(), "resp");
								GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
							} else { // no dlr will be received. process related pdu
								lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "E", "FAILED",
										hlrRequest.getResponse().getCommandStatus() + "", "SUBMIT_ERROR",
										new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 0 + "", "resp");
								GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
							}
						}
					} else if (hlrRequest.getResponse().getCommandId() == Data.DELIVER_SM) {
						RouteObject routeObject = null;
						HlrResponse result = hlrRequest.getResponse();
						String status = result.getStatus();
						if (GlobalVar.EnqueueRouteObject.containsKey(hlrRequest.getMessageId())) {
							routeObject = GlobalVar.EnqueueRouteObject.remove(hlrRequest.getMessageId());
						} else {
							logger.info(hlrRequest.getMessageId() + " RouteObject Not Found[Dlr].");
							System.out.println(
									systemId + " < Lookup Status[" + hlrRequest.getMessageId() + "] > " + status);
							lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "T", result.getStatus(),
									result.getNnc(), result.isPorted(), result.getPortedNNC(), result.isRoaming(),
									result.getRoamingNNC(), result.getError(),
									LookupStatus.getErrorName(result.getError()),
									new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "dlr");
							GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
							continue;
						}
						String destination = routeObject.getDestAddress();
						System.out.println(systemId + " : " + destination + " < Lookup Status["
								+ hlrRequest.getMessageId() + "] > " + status);
						lookupDTO = new LookupDTO(hlrRequest.getMessageId(), "T", result.getStatus(), result.getNnc(),
								result.isPorted(), result.getPortedNNC(), result.isRoaming(), result.getRoamingNNC(),
								result.getError(), LookupStatus.getErrorName(result.getError()),
								new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), "dlr");
						GlobalVar.lookupStatusInsertQueue.enqueue(lookupDTO);
						if (result.isPorted() && routeObject.isMms()) {
							logger.info(routeObject.getMsgId() + " Ported Not Applicable For Mms: "
									+ routeObject.getSmsc());
							result.setPorted(false);
						}
						if (result.isPorted() && routeObject.isMnp()) {
							if (status.equalsIgnoreCase("UNDELIV")) {
								status = "DELIVRD";
							}
						}
						if (status.equalsIgnoreCase("UNDELIV")) {
							if (result.getPermanent() == 5) {
								routeObject.setSmsc(com.hti.util.Constants.HLR_DOWN_SMSC_5);
							} else if (result.getPermanent() == 4) {
								routeObject.setSmsc(com.hti.util.Constants.HLR_DOWN_SMSC_4);
							} else if (result.getPermanent() == 3) {
								routeObject.setSmsc(com.hti.util.Constants.HLR_DOWN_SMSC_3);
							} else if (result.getPermanent() == 2) {
								routeObject.setSmsc(com.hti.util.Constants.HLR_DOWN_SMSC_2);
							} else {
								routeObject.setSmsc(com.hti.util.Constants.HLR_DOWN_SMSC_1);
							}
							routeObject.setGroupId(0);
							// smscChanged = true;
						} else {
							if (result.isDnd()) {
								// check if sender is promo
								String senderid = routeObject.getSourceAddress();
								for (String filter : com.hti.util.Constants.PROMO_SENDER.split(",")) {
									if (Pattern.compile("(?i)" + filter).matcher(senderid).find()) {
										logger.info(routeObject.getMsgId() + " Promo Sender[" + senderid + "] matched["
												+ filter + "] " + routeObject.getSmsc() + " -> "
												+ com.hti.util.Constants.DND_SMSC);
										result.setPorted(false);
										routeObject.setSmsc(com.hti.util.Constants.DND_SMSC);
										routeObject.setGroupId(0);
										break;
									}
								}
							}
							if (result.isPorted()) {
								if (!routeObject.isRerouted()) {
									if (routeObject.getGroupId() == 0 && (routeObject.getSmsc()
											.equalsIgnoreCase(GlobalVars.REJECT_SMSC)
											|| routeObject.getSmsc().equalsIgnoreCase(GlobalVars.DELIVRD_SMSC)
											|| routeObject.getSmsc().equalsIgnoreCase(GlobalVars.UNDELIV_SMSC))) {
										logger.info(routeObject.getMsgId() + " Ported Route Not Changed: "
												+ routeObject.getSmsc());
									} else {
										String ported_nnc = result.getPortedNNC();
										RoutingDTO ported_routing = null;
										try {
											int mcc = Integer.parseInt(ported_nnc.substring(0, 3));
											int mnc = Integer.parseInt(ported_nnc.substring(3, ported_nnc.length()));
											ported_routing = getRouting(mcc + "" + mnc);
											if (ported_routing != null) {
												logger.info(systemId + " : " + destination + " <  PortedNNC:"
														+ ported_nnc + " > < Route:" + routeObject.getSmsc()
														+ " PortedRoute:" + ported_routing.getSmsc() + " >"
														+ " recordMnp: " + recordMnp);
												if (recordMnp) {
													routeObject.setNetworkId(ported_routing.getNetworkId());
												}
												if ((ported_routing.getRegisterSenderId() != null
														&& !ported_routing.getRegisterSenderId().isEmpty())
														&& (ported_routing.getRegisterSmsc() != null
																&& ported_routing.getRegisterSmsc().length() > 0)) {
													String senderid = routeObject.getSourceAddress();
													if (ported_routing.getRegisterSenderId()
															.contains(senderid.toLowerCase())) {
														routeObject.setSmsc(ported_routing.getRegisterSmsc());
														routeObject.setGroupId(0);
														logger.info(systemId + "<----Registered Sender Found ["
																+ senderid + "-> " + ported_routing.getRegisterSmsc()
																+ " ]----->");
													} else {
														routeObject.setSmsc(ported_routing.getSmsc());
													}
												} else {
													routeObject.setSmsc(ported_routing.getSmsc());
												}
												// -------- calculate cost as per ported routing -----------
												if (ported_routing.getCost() > 0) {
													double ported_cost = ported_routing.getCost();
													if (routeObject.getPartNumber() <= 1) {
														ported_cost = ported_cost + ported_routing.getHlrCost();
													}
													if (ported_cost == routeObject.getCost()) {
														logger.debug(systemId + "[" + routeObject.getMsgId()
																+ "] ported cost[" + ported_cost
																+ "] is equal to deducted cost[" + routeObject.getCost()
																+ "].");
													} else {
														if (ported_cost > routeObject.getCost()) {
															logger.debug(systemId + "[" + routeObject.getMsgId()
																	+ "] ported cost[" + ported_cost
																	+ "] is greater then deducted cost["
																	+ routeObject.getCost() + "].");
															deduct(systemId, ported_cost - routeObject.getCost());
														} else {
															logger.debug(systemId + "[" + routeObject.getMsgId()
																	+ "] ported cost[" + ported_cost
																	+ "] is less then deducted cost["
																	+ routeObject.getCost() + "].");
															doRefund(systemId, routeObject.getCost() - ported_cost);
														}
														routeObject.setCost(ported_cost);
													}
												}
											} else {
												logger.info(systemId + " : " + destination + " < PortedNNC:"
														+ ported_nnc + " > < Route:" + routeObject.getSmsc()
														+ "> PortedRoute Not Configured ");
											}
										} catch (Exception ex) {
											logger.error("portedNNC: " + ported_nnc + "" + ex);
										}
									}
								} else {
									logger.info(routeObject.getMsgId() + " Ported But Already Rerouted: "
											+ routeObject.getSmsc());
								}
							}
						}
						GlobalQueue.smsc_in_temp_update_Queue
								.enqueue(new SmscInObj(routeObject.getMsgId(), "C", routeObject.getSmsc(),
										routeObject.getGroupId(), routeObject.getCost(), routeObject.getNetworkId()));
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		logger.info(systemId + "_" + threadId + " ResponseHandler Stopped.Queue: " + responeQueue.size());
	}

	private boolean doRefund(String to, double amount) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(to);
			if (balance != null) {
				logger.debug(to + " Wallet Flag: " + balance.getFlag());
				if (balance.getFlag().equalsIgnoreCase("No")) { // no need for credit mode
					return true;
				} else {
					return balance.refundAmount(amount);
				}
			} else {
				return false;
			}
		} catch (EntryNotFoundException ex) {
			logger.error(to + " <-- Balance Entry Not Found [" + amount + "] --> ");
			return false;
		} catch (Exception ex) {
			logger.info(to + " <-- Balance refund Error [" + amount + "] --> " + ex);
			return false;
		}
	}

	private boolean deduct(String to, double amount) {
		try {
			UserBalance balance = GlobalVars.userService.getUserBalance(to);
			if (balance != null) {
				logger.debug(to + " Wallet Flag: " + balance.getFlag());
				if (balance.getFlag().equalsIgnoreCase("No")) { // no need for credit mode
					return true;
				} else {
					return balance.deductAmount(amount);
				}
			} else {
				return false;
			}
		} catch (EntryNotFoundException ex) {
			logger.error(to + " <-- Balance Entry Not Found [" + amount + "] --> ");
			return false;
		} catch (Exception ex) {
			logger.info(to + " <-- Balance refund Error [" + amount + "] --> " + ex);
			return false;
		}
	}

	private RoutingDTO getRouting(String mccmnc) {
		RoutingDTO routing = null;
		int networkCode = 0;
		if (NetworkMap == null) {
			NetworkMap = new HashMap<String, Integer>(GlobalCache.NncMapping);
		}
		if (RoutingMap == null) {
			logger.debug(systemId + " Checking Route For HLRResponseHandler[" + mccmnc + "]");
			RoutingMap = GlobalVars.routeService.listRouting(systemId);
		}
		if (RoutingMap != null) {
			if (!RoutingMap.isEmpty()) {
				if (NetworkMap.containsKey(mccmnc)) {
					networkCode = NetworkMap.get(mccmnc);
					if (RoutingMap.containsKey(networkCode)) {
						routing = (RoutingDTO) RoutingMap.get(networkCode);
					}
				}
			}
		}
		return routing;
	}

	public void stop() {
		stop = true;
		logger.info(systemId + "_" + threadId + " ResponseHandler Stopping.Queue: " + responeQueue.size());
	}
}
