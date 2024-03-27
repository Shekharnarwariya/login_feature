package com.hti.smpp.common.flag;

import com.hti.util.GlobalVar;

public class FlagUpdateThread implements Runnable {
	private final long delay;

	public FlagUpdateThread(long delay) {
		this.delay = delay;

	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				// Check if the IMap is not empty
				if (!GlobalVar.flag_write_Cache.isEmpty()) {
					// Iterate over all entries in the map and print each FlagDTO
					for (FlagDTO flagDTO : GlobalVar.flag_write_Cache.values()) {
						System.out.println(flagDTO);
					}
				} else {
					System.out.println("The map is currently empty.");
				}

				// Sleep for the specified delay before the next print
				Thread.sleep(delay);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt(); // Restore interrupted status
			System.out.println("Thread was interrupted, stopping...");
		}
	}
}
