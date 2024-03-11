package com.hti.objects;

import java.util.LinkedList;
import java.util.ListIterator;

import com.logica.smpp.SmppObject;

public class SerialQueue extends SmppObject {
	private int maxQueueSize = 0;
	private LinkedList queueData = new LinkedList();
	private Object mutex;
	private boolean send = false;

	public SerialQueue() {
		mutex = this;
	}

	public SerialQueue(int maxSize) {
		maxQueueSize = maxSize;
		mutex = this;
	}

	/**
	 * Current count of the elements in the queue.
	 */
	public int size() {
		synchronized (mutex) {
			return queueData.size();
		}
	}

	/**
	 * If there is no element in the queue.
	 */
	public boolean isEmpty() {
		synchronized (mutex) {
			return queueData.isEmpty();
		}
	}

	/**
	 * Removes first element form the queue and returns it. If the queue is empty, returns null.
	 */
	public Object dequeue() {
		synchronized (mutex) {
			Object first = null;
			/// Added by arshad
			while (this.isEmpty()) {
				try {
					// System.out.println("Sleeping"+Thread.currentThread().getName());
					wait();
					// System.out.println("Wake up"+Thread.currentThread().getName());
				} catch (InterruptedException e) {
					// System.out.println("Interrupted "+Thread.currentThread().getName());
					return null;
				}
			}
			///
			if (size() > 0) {
				first = queueData.removeFirst();
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
			if ((maxQueueSize > 0) && (size() >= maxQueueSize)) {
				throw new IndexOutOfBoundsException("Queue is full. Element not added.");
			}
			queueData.add(obj);
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
