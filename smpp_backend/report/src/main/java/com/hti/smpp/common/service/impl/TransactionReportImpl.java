package com.hti.smpp.common.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.dto.UserEntryExt;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.request.UserEntryForm;
import com.hti.smpp.common.response.TransactionResponse;
import com.hti.smpp.common.service.TransactionReportService;
import com.hti.smpp.common.service.UserDAService;
import com.hti.smpp.common.user.dto.BalanceEntry;
import com.hti.smpp.common.user.dto.RechargeEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.BalanceEntryRepository;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;

import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;

@Service
public class TransactionReportImpl implements TransactionReportService {

	@Autowired
	private UserDAService userService;
	@Autowired
	private UserEntryRepository userRepository;
	@Autowired
	private BalanceEntryRepository balanceEntryRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Override
	public ResponseEntity<?> executeTransaction(String username) {
		try {
			final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);
			String target = IConstants.FAILURE_KEY;
			Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
			UserEntry user = userOptional.orElseThrow(() -> new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username })));
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
			String accessRole = user.getRole();
			Integer[] userid = null;
			boolean proceed = true;
			if (accessRole.equalsIgnoreCase("user")) {
				userid = new Integer[1];
				userid[0] = user.getId();
			} else {
				UserEntryForm userForm = new UserEntryForm();
				if (userForm == null || userForm.getUserids() == null) {
					userid = new Integer[1];
					userid[0] = user.getId();
					logger.info(user.getSystemId() + " View Transaction Request For: " + user.getId());
				} else {
					if (user.getRole().equalsIgnoreCase("admin")) {
						Map<Integer, String> users = userService.listUsersUnderMaster(user.getSystemId());
						Predicate<Integer, WebMasterEntry> p = new PredicateBuilderImpl().getEntryObject()
								.get("secondaryMaster").equal(user.getSystemId());
						for (WebMasterEntry webEntry : GlobalVars.WebmasterEntries.values(p)) {
							UserEntry userEntry = GlobalVars.UserEntries.get(webEntry.getUserId());
							users.put(userEntry.getId(), userEntry.getSystemId());
						}
						for (int user_id : userForm.getUserids()) {
							if (user_id == user.getId()) {
								logger.info(user.getSystemId() + " self Transaction list request");
							} else {
								logger.info(user.getSystemId() + " Transaction list request For : " + user_id);
								if (!users.containsKey(user_id)) {
									logger.info(user.getSystemId() + "[" + user.getRole() + "] Invalid User[" + user_id
											+ "] Transaction Request");
									proceed = false;
									break;
								}
							}
						}
					}
					if (proceed) {
						userid = userForm.getUserids();

					}
				}
			}
			if (proceed) {
				List<RechargeEntry> txnlist = new ArrayList<RechargeEntry>();
				Collection<List<RechargeEntry>> transactions = userService.listTransactions(userid).values();

				TransactionResponse response = new TransactionResponse();

				if (userid.length == 1) {
					UserEntry userEntry = userRepository.findById(userid[0]).get();

					Optional<BalanceEntry> balanceOptional = balanceEntryRepository.findByUserId(userid[0]);

					BalanceEntry balance;
					if (balanceOptional.isPresent()) {
						balance = balanceOptional.get();
					} else {
						throw new EntityNotFoundException("Balance entry not found for user ID: " + userid[0]);
					}
					UserEntryExt entry = new UserEntryExt(userEntry);
					entry.setBalance(balance);
					boolean wallet_mode = true;
					if (balance.getWalletFlag().equalsIgnoreCase("no")) {
						wallet_mode = false;
					}
					double totalCreditAmount = 0, totalDebitAmount = 0;
					for (List<RechargeEntry> list : transactions) {
						for (RechargeEntry rechargeEntry : list) {
							if (rechargeEntry.getParticular() != null) {
								if (wallet_mode) {
									if (rechargeEntry.getParticular().equalsIgnoreCase("cr")) {
										totalCreditAmount += rechargeEntry.getToBeAddedWallet();
									} else {
										totalDebitAmount += rechargeEntry.getToBeAddedWallet();
									}
								} else {
									if (rechargeEntry.getParticular().equalsIgnoreCase("cr")) {
										totalCreditAmount += rechargeEntry.getToBeAddedCedits();
									} else {
										totalDebitAmount += rechargeEntry.getToBeAddedCedits();
									}
								}
							}
						}
						txnlist.addAll(list);
					}
					response.setRechargeEntries(txnlist);
					response.setBalanceEntry(balance);
					response.setTotalCreditAmount(totalCreditAmount);
					response.setTotalDebitAmount(totalDebitAmount);
					 response.setCurrency(userEntry.getCurrency());
				} else {
					for (List<RechargeEntry> list : transactions) {
						txnlist.addAll(list);
					}
				}
				response.setRechargeEntries(txnlist);
				logger.info(user.getSystemId() + " View Transaction list: " + txnlist.size());
				target = IConstants.SUCCESS_KEY;
				return ResponseEntity.ok(response);
			} else {
				throw new NotFoundException(
						messageResourceBundle.getExMessage(ConstantMessages.TRASACTION_DATA_NOT_FOUND));
			}
		} catch (NotFoundException e) {

			throw new NotFoundException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(messageResourceBundle
					.getExMessage(ConstantMessages.BAD_REQUEST_EXCEPTION_MESSAGE, new Object[] { e.getMessage() }));

		} catch (Exception e) {
			throw new InternalServerException(e.getMessage());
		}
	}
}
