package com.hti.init;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.dao.UserDAService;
import com.hti.dao.impl.UserDAServiceImpl;
import com.hti.database.DBService;
import com.hti.user.dto.BalanceEntry;
import com.hti.util.Constants;
import com.hti.util.FileUtil;
import com.hti.util.FlagStatus;
import com.hti.util.GlobalVar;

public class BalanceOperator implements Runnable {
	private Logger logger = LoggerFactory.getLogger(BalanceOperator.class);
	private boolean stop;
	private Set<Integer> MinBalanceUser = new HashSet<Integer>();
	private DBService dbservice = null;

	public BalanceOperator() {
		logger.info("BalanceOperator Starting");
		dbservice = new DBService();
	}

	@Override
	public void run() {
		while (!stop) {
			System.out.println("BalanceOperator Running");
			if (GlobalVar.MASTER_MEMBER) {
				try {
					for (BalanceEntry entry : GlobalVar.balance_entries.values()) {
						try {
							if (entry.isActive()) {
								// System.out.println(entry.toString());
								dbservice.updateBalance(entry);
								if (entry.getWalletFlag().equalsIgnoreCase("MIN")) {
									if (!MinBalanceUser.contains(entry.getUserId())) {
										logger.info(entry.getSystemId() + " Marked MinBalance User");
										MinBalanceUser.add(entry.getUserId());
									}
								} else {
									if (MinBalanceUser.contains(entry.getUserId())) {
										logger.info(entry.getSystemId() + " Unmarked MinBalance User");
										MinBalanceUser.remove(entry.getUserId());
										FileUtil.setFlag(Constants.USER_FLAG_DIR + entry.getSystemId() + ".txt",
												FlagStatus.BALANCE_REFRESH);
										FileUtil.setRefreshFlag(Constants.CLIENT_FLAG_FILE);
									}
								}
							}
						} catch (Exception e) {
							logger.error(entry.getSystemId(), e);
						}
					}
				} catch (Exception e) {
					logger.error("", e.fillInStackTrace());
				}
			}
			try {
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e) {
			}
		}
		logger.info("BalanceOperator Stopped");
	}

	public void stop() {
		logger.info("BalanceOperator Stopping");
		stop = true;
	}
}
