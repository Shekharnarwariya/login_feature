/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 */
public class CheckCpuCycle implements Runnable {
	private Logger logger = LoggerFactory.getLogger(CheckCpuCycle.class);
	// private long gc_limit = 1024
	public static boolean EXECUTE_GC = false;
	private long mb = 1024 * 1024;
	private Runtime runtime = Runtime.getRuntime();
	private long used_memory;
	private long max_memory;
	// long gc_limit;
	private long proc_limit;
	private boolean stop;

	public CheckCpuCycle() {
		logger.info("CheckCpuCycle Starting");
	}

	@Override
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(1 * 60 * 1000); // 1 minutes
			} catch (Exception ex) {
			}
			checkMemoryUsage();
			if (used_memory >= proc_limit) {
				EXECUTE_GC = true;
			}
			if (EXECUTE_GC) {
				EXECUTE_GC = false;
				runGC();
			}
		}
		logger.info("CheckCpuCycle Stopped");
	}

	private void runGC() {
		logger.warn("Excuting Garbage Collector. Used Memory:-> " + used_memory + " MB"); // Print used memory
		System.gc();
		used_memory = ((runtime.totalMemory() - runtime.freeMemory()) / mb);
		logger.info("After Garbage Collection. Used Memory:-> " + used_memory + " MB");
	}

	private void checkMemoryUsage() {
		used_memory = (runtime.totalMemory() - runtime.freeMemory()) / mb;
		max_memory = (runtime.maxMemory() / mb);
		proc_limit = (max_memory * 8) / 10; // 80% memeory
		logger.info("Memory Used:---> " + used_memory + " MB. Max Available: " + max_memory + " MB");
	}

	public void stop() {
		logger.info("CheckCpuCycle Stopping");
		stop = true;
	}
}
