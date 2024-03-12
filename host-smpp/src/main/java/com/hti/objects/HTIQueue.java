/*
 * Copyright (c) 1996-2001
 * Logica Mobile Networks Limited
 * All rights reserved.
 *
 * This software is distributed under Logica Open Source License Version 1.0
 * ("Licence Agreement"). You shall use it and distribute only in accordance
 * with the terms of the License Agreement.
 *
 */
package com.hti.objects;

import java.util.LinkedList;
import java.util.ListIterator;

import com.logica.smpp.SmppObject;

/**
 * Implements fifo style queue with removal of specified element from inside of the queue.
 *
 * @author Logica Mobile Networks SMPP Open Source Team
 * @version 1.2, 1 Oct 2001
 */
/*
 * 13-07-01 ticp@logica.com added dequeue(Object) method which finds and removes object which equals() to the provided one (not too queue-ish)
 * 01-10-01 ticp@logica.com added some javadoc comments
 */
public class HTIQueue extends SmppObject {
	// private int maxQueueSize = 0;
	private int maxHoldSize = 50000;
	private LinkedList queueData = new LinkedList();
	private LinkedList tempQueueData = new LinkedList();
	private Object mutex;
	private boolean send = false;

	public HTIQueue() {
		mutex = this;
	}

	/*
	 * public HTIQueue(int maxSize) { maxQueueSize = maxSize; mutex = this; }
	 */
	/**
	 * Current count of the elements in the queue.
	 */
	public int size() {
		synchronized (mutex) {
			return queueData.size() + tempQueueData.size();
		}
	}

	/**
	 * If there is no element in the queue.
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			if (queueData.isEmpty() && tempQueueData.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Removes first element form the queue and returns it. If the queue is empty, returns null.
	 */
	public Object dequeue() {
		synchronized (mutex) {
			Object first = null;
			while (this.isEmpty()) {
				try {
					// logger.info("Sleeping"+Thread.currentThread().getName());
					wait();
					// logger.info("Wake up"+Thread.currentThread().getName());
				} catch (InterruptedException e) {
					// logger.info("Interrupted "+Thread.currentThread().getName());
					return null;
				}
			}
			///
			if (queueData.size() > 0) {
				first = queueData.removeFirst();
			} else if (tempQueueData.size() > 0) {
				first = tempQueueData.removeFirst();
			}
			return first;
		}
	}

	/**
	 * Tries to find the provided element in the queue and if found, removes it from the queue and returns it. If the element is not found returns
	 * null.
	 */
	public Object dequeue(Object obj) {
		Object found = null;
		synchronized (mutex) {
			found = find(obj);
			if (found != null) {
				queueData.remove(found);
			}
		}
		return found;
	}

	/**
	 * Appends an element to the end of the queue. If the queue has set limit on maximum elements and there is already specified max count of elements
	 * in the queue throws IndexOutOfBoundsException.
	 */
	public void enqueue(Object obj) throws IndexOutOfBoundsException {
		synchronized (mutex) {
			/*
			 * if ((maxQueueSize > 0) && (queueData.size() >= maxQueueSize)) { throw new
			 * IndexOutOfBoundsException("Queue is full. Element not added."); }
			 */
			if (queueData.size() >= maxHoldSize) {
				tempQueueData.add(obj);
			} else {
				queueData.add(obj);
			}
			notifyAll(); // Added by arshad
		}
	}

	/**
	 * Searches the queue to find the provided element. Uses <code>equals</code> method to compare elements.
	 */
	public Object find(Object obj) {
		synchronized (mutex) {
			Object current;
			ListIterator iter = queueData.listIterator(0);
			while (iter.hasNext()) {
				current = iter.next();
				if (current.equals(obj)) {
					return current;
				}
			}
		}
		return null;
	}

	public synchronized void setTrue() {
		send = true;
	}

	public synchronized boolean getFlag() {
		return send;
	}

	public synchronized void setFalse() {
		send = false;
	}
}
