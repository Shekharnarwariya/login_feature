/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 */
public class FileUtil {
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	private static Map<String, String> UserFlagValue = new HashMap<String, String>(); // clients flag values

	public static Properties readProperties(String filename) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(filename);
			props.load(fileInputStream);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ioe) {
					fileInputStream = null;
				}
			}
		}
		return props;
	}
	
	public static Properties readProperties(String filename, boolean create) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		FileInputStream fileInputStream = null;
		File file = new File(filename);
		if (!file.exists()) {
			logger.info(filename + " Created: " + file.createNewFile());
		}
		try {
			fileInputStream = new FileInputStream(filename);
			props.load(fileInputStream);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException ioe) {
					fileInputStream = null;
				}
			}
		}
		return props;
	}

	public static String readFlag(String path, boolean isCreate) {
		String flagValue = FlagStatus.DEFAULT;
		try {
			flagValue = readValue(path, "FLAG");
		} catch (FileNotFoundException fnfex) {
			logger.info("File Not Found :" + path);
			if (isCreate) {
				logger.info("Creating File :" + path);
				setDefaultFlag(path);
			}
		} catch (IOException ioe) {
			logger.error(path, ioe.fillInStackTrace());
		}
		return flagValue;
	}

	public static void setDefaultFlag(String path) {
		setDefault(path, "FLAG = " + FlagStatus.DEFAULT);
	}

	public static void setRefreshFlag(String path) {
		setDefault(path, "FLAG = " + FlagStatus.REFRESH);
	}

	public static void setFlag(String path, String content) {
		setDefault(path, content);
	}

	public static boolean setDefault(String path, String content) {
		FileOutputStream fileOutputStream = null;
		boolean done = false;
		try {
			fileOutputStream = new FileOutputStream(path);
			fileOutputStream.write(content.getBytes());
			done = true;
		} catch (IOException ex) {
			logger.error("setDefault()" + path, ex);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException ioe) {
					fileOutputStream = null;
				}
			}
		}
		return done;
	}

	public static String readValue(String path, String param) throws FileNotFoundException, IOException {
		String value = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(path));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(param)) {
					value = line.substring(line.indexOf("=") + 1, line.length()).trim();
					break;
				}
			}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					reader = null;
				}
			}
		}
		return value;
	}

	public static String readContent(String filename) {
		String content = "";
		BufferedReader reader = null;
		File file = new File(filename);
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				content += "\n" + line;
			}
		} catch (FileNotFoundException ex) {
			logger.error("readContent(" + filename + ")", ex);
		} catch (IOException ex) {
			logger.error("readContent(" + filename + ")", ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException ioe) {
					reader = null;
				}
			}
		}
		return content;
	}

	private static boolean writeContent(String filename, String content, boolean append) throws IOException {
		FileOutputStream fileOutputStream = null;
		boolean done = false;
		try {
			fileOutputStream = new FileOutputStream(new File(filename), append);
			fileOutputStream.write(content.getBytes());
			done = true;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException ioe) {
				}
			}
		}
		return done;
	}

	/*
	 * public static void writeLog(String filename, String content) { try { calculateFileSize(filename, 5000); writeContent(filename, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) +
	 * ": " + content + "\n", true); } catch (IOException ioe) { logger.error(filename + ": " + ioe); ioe.printStackTrace(); } }
	 */
	public static void writeQueueLog(String filename, String content) {
		try {
			calculateFileSize(filename, 5000);
			writeContent(filename, content, true);
		} catch (IOException ioe) {
			logger.error(filename + ": " + ioe);
			ioe.printStackTrace();
		}
	}

	public static boolean writePduLog(String filename, String content) {
		PrintStream printStream = null;
		boolean done = false;
		try {
			calculateFileSize(filename, 50000);
			printStream = new PrintStream(new FileOutputStream(filename, true), true, "UTF-16");
			printStream.print(content);
			done = true;
		} catch (Exception ioe) {
			logger.error(filename + ": " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	private static void calculateFileSize(String filename, long limit) throws IOException {
		long fileSize = 0;
		File file = null;
		boolean renamed = false, created = false;
		file = new File(filename);
		if (!file.isFile() || !file.exists()) {
			created = file.createNewFile();
			if (created) {
				System.out.println("File Created :: " + filename);
			} else {
				logger.error("Error in File Creation :: " + filename);
			}
		} else {
			fileSize = file.length();
			fileSize = fileSize / 1024;
			if (fileSize >= limit) {
				System.out.println("FILE SIZE(" + filename + ")::" + fileSize + "KB");
				String extension = "", newFileName = "";
				String currentDate = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date());
				extension = filename.substring(filename.indexOf(".") + 1, filename.length());
				newFileName = filename.substring(0, filename.indexOf("."));
				newFileName = newFileName + "_" + currentDate + "." + extension;
				File newFile = new File(newFileName);
				renamed = file.renameTo(newFile);
				if (renamed) {
					file = new File(filename);
					file.createNewFile();
				}
			}
		}
	}
}
