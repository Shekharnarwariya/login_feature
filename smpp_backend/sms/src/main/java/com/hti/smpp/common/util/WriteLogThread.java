/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.logica.smpp.util.Queue;

/**
 *
 * @author Administrator
 */
public class WriteLogThread implements Runnable {
	public static Queue logQueue = new Queue();
	private boolean isStop = false;

	public WriteLogThread() {
		System.out.println("<-- WriteLogThread Starting --> ");
		new Thread(this, "WriteLogThread").start();
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
					MultiUtility.writeHttpLog(respContent);
				}
				content = null;
				respContent = null;
			}
		}
		System.out.println("<-- WriteLogThread Stopped --> ");
	}

	public void stop() {
		System.out.println("<-- WriteLogThread Stopping --> ");
		isStop = true;
	}
}
