package com.hti.smpp.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class FlagUtil {
	
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
