package com.hti.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.exception.EntryNotFoundException;
import com.hti.user.dto.BalanceEntry;
import com.hti.util.GlobalCache;

public class UserBalance {
	private int user_id;
	private BalanceEntry balanceEntry;
	private Logger logger = LoggerFactory.getLogger("userLogger");

	public UserBalance(int user_id) throws EntryNotFoundException {
		this.user_id = user_id;
		setActive(true);
		logger.info(user_id + " Balance Object Created");
	}

	public synchronized String getFlag() throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			return GlobalCache.BalanceEntries.get(user_id).getWalletFlag();
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized boolean deductAmount(double cost) throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			boolean to_return = false;
			try {
				GlobalCache.BalanceEntries.lock(user_id);
				balanceEntry = GlobalCache.BalanceEntries.get(user_id);
				if (balanceEntry.getWalletAmount() > cost) {
					balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() - cost);
					to_return = true;
				} else {
					balanceEntry.setWalletFlag("MIN");
					logger.error(user_id + " Insufficient Balance: " + balanceEntry.getWalletAmount());
				}
				GlobalCache.BalanceEntries.put(user_id, balanceEntry);
			} finally {
				GlobalCache.BalanceEntries.unlock(user_id);
			}
			return to_return;
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized boolean deductCredit(int credit) throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			try {
				GlobalCache.BalanceEntries.lock(user_id);
				balanceEntry = GlobalCache.BalanceEntries.get(user_id);
				if (balanceEntry.getCredits() > credit) {
					balanceEntry.setCredits(balanceEntry.getCredits() - credit);
					GlobalCache.BalanceEntries.put(user_id, balanceEntry);
					return true;
				} else {
					logger.error(user_id + " Insufficient Credits: " + balanceEntry.getCredits());
				}
			} finally {
				GlobalCache.BalanceEntries.unlock(user_id);
			}
			return false;
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized boolean refundAmount(double cost) throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			try {
				GlobalCache.BalanceEntries.lock(user_id);
				balanceEntry = GlobalCache.BalanceEntries.get(user_id);
				if (balanceEntry.getWalletFlag().equalsIgnoreCase("MIN")) {
					balanceEntry.setWalletFlag("Yes");
				}
				balanceEntry.setWalletAmount(balanceEntry.getWalletAmount() + cost);
				GlobalCache.BalanceEntries.put(user_id, balanceEntry);
			} finally {
				GlobalCache.BalanceEntries.unlock(user_id);
			}
			return true;
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized boolean refundCredit(int credit) throws EntryNotFoundException {
		logger.debug(user_id + " refundCredit: " + credit);
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			try {
				GlobalCache.BalanceEntries.lock(user_id);
				balanceEntry = GlobalCache.BalanceEntries.get(user_id);
				balanceEntry.setCredits(balanceEntry.getCredits() + credit);
				logger.debug(user_id + " EffectiveCredits: " + balanceEntry.getCredits());
				GlobalCache.BalanceEntries.put(user_id, balanceEntry);
			} finally {
				GlobalCache.BalanceEntries.unlock(user_id);
			}
			return true;
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized long getCredit() throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			return GlobalCache.BalanceEntries.get(user_id).getCredits();
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized double getAmount() throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			return GlobalCache.BalanceEntries.get(user_id).getWalletAmount();
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized boolean isActive() throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			return GlobalCache.BalanceEntries.get(user_id).isActive();
		} else {
			throw new EntryNotFoundException();
		}
	}

	public synchronized void setActive(boolean active) throws EntryNotFoundException {
		if (GlobalCache.BalanceEntries.containsKey(user_id)) {
			try {
				GlobalCache.BalanceEntries.lock(user_id);
				balanceEntry = GlobalCache.BalanceEntries.get(user_id);
				balanceEntry.setActive(active);
				GlobalCache.BalanceEntries.put(user_id, balanceEntry);
			} finally {
				GlobalCache.BalanceEntries.unlock(user_id);
			}
		} else {
			throw new EntryNotFoundException();
		}
	}
}
