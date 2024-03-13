package com.hti.smpp.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.smpp.util.Queue;

public class WriteXmlLogThread implements Runnable {
	public static Queue logQueue = new Queue();
	private Logger logger = LoggerFactory.getLogger(WriteXmlLogThread.class);
	private boolean isStop = false;

	public WriteXmlLogThread() {
		logger.info("<-- WriteXmlLogThread Starting --> ");
		new Thread(this, "WriteXmlLogThread").start();
	}

	@Override
	public void run() {
		String content = null;
		String respContent = "";
		while (!isStop) {
			if (logQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			} else {
				int counter = 0;
				respContent = "";
				while (!logQueue.isEmpty()) {
					content = (String) logQueue.dequeue();
					respContent += new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + " [ " + content
							+ " ]" + "\n";
					if (++counter >= 1000) {
						break;
					}
				}
				if (respContent.length() > 0) {
					MultiUtility.writeXmlLog(respContent);
				}
				content = null;
				respContent = null;
			}
		}
		logger.info("<-- WriteXMLLogThread Stopped --> ");
	}

	public void stop() {
		logger.info("<-- WriteXMLLogThread Stopping -->");
		isStop = true;
	}
}
