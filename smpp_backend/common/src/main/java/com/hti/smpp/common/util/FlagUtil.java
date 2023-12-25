package com.hti.smpp.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/**
 * Utility class for reading and changing flag values in a file.
 */

public class FlagUtil {
	

    /**
     * Read the flag value from the specified file.
     *
     * @param path The path to the file containing the flag.
     * @return The read flag value.
     */
	
	public static String readFlag(String path) {
        String flagValue = FlagStatus.DEFAULT;

        try (Scanner scanner = new Scanner(Path.of(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("FLAG")) {
                    flagValue = line.substring(line.indexOf("=") + 1).trim();
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Exception in readFlag(): " + ex.getLocalizedMessage());
        }

        return flagValue;
    }
	
	 /**
     * Change the flag value in the specified file.
     *
     * @param path      The path to the file containing the flag.
     * @param flagValue The new flag value to set.
     * @return True if the flag value was successfully changed, false otherwise.
     */
	
	public static boolean changeFlag(String path, String flagValue) {
        String writeData = "FLAG = " + flagValue;
        Path filePath = Path.of(path);

        try {
            // Write the new flag value to the file using Files.writeString
            Files.writeString(filePath, writeData, StandardOpenOption.WRITE);
            System.out.println("[" + path + "] Flag Set to: " + flagValue);
            return true;
        } catch (IOException ex) {
            System.out.println("Exception in changeFlag(): " + ex.getLocalizedMessage());
            return false;
        }
    }

}
