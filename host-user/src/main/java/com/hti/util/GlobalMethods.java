package com.hti.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.hlr.GlobalVar;
import com.hti.hlr.HlrRequestHandler;
import com.logica.smpp.util.Queue;

public class GlobalMethods {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");

	public static synchronized Queue getHLRQueueProcess(String systemId) {
		try {
			if (GlobalVar.HlrRequestHandlers.containsKey(systemId)) {
				logger.info(systemId + "_" + Thread.currentThread().getId() + " HlrRequestHandler Exist");
				return GlobalVar.HlrRequestHandlers.get(systemId).getHlrQueue();
			} else {
				logger.info(systemId + "_" + Thread.currentThread().getId() + " Creating HlrRequestHandler");
				Queue hlrQueue = new Queue();
				HlrRequestHandler hlrRequestHandler = new HlrRequestHandler(systemId,
						GlobalVars.userService.getUserEntry(systemId).getPassword());
				hlrRequestHandler.setHlrQueue(hlrQueue);
				GlobalVar.HlrRequestHandlers.put(systemId, hlrRequestHandler);
				hlrRequestHandler.startHandler();
				return hlrQueue;
			}
		} catch (Exception e) {
			logger.error(systemId, e);
		}
		return null;
	}
}
