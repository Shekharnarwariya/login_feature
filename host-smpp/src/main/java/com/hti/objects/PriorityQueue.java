//Implemented By Rajeev@Admin Dated 02/07/05
package com.hti.objects;

import java.io.Serializable;

public class PriorityQueue implements Serializable {
	public HTIQueue[] PQueue;
	int totalqueue;

	public PriorityQueue(int noofQueue) {
		totalqueue = noofQueue;
		PQueue = new HTIQueue[noofQueue + 1];
		// logger.info(" before Priority Queue Initialized");
		for (int i = 1; i <= noofQueue; i++) {
			try {
				PQueue[i] = new HTIQueue();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// this.PQueue[1]=new HTIQueue();
		}
		// logger.info("Priority Queue Initialized");
	}

	public int size() {
		int toreturn = 0;
		for (int i = 1; i <= totalqueue; i++) {
			if (PQueue[i].isEmpty()) {
				continue;
			} else {
				toreturn += PQueue[i].size();
				// break;
			}
		}
		return toreturn;
	}

	public boolean isEmpty() {
		boolean isEmpty = true;
		for (int i = 1; i <= totalqueue; i++) {
			if (PQueue[i].isEmpty()) {
				continue;
			} else {
				isEmpty = false;
				break;
			}
		}
		return isEmpty;
	}
}
