package com.hti.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

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

	public static String readFlag(String path, boolean isCreate) {
		String flagValue = "100";
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

	public static Properties readSmscFlag(String path, boolean isCreate) {
		Properties props = new Properties();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(path);
			props.load(fileInputStream);
		} catch (FileNotFoundException e) {
			logger.info("File Not Found :" + path);
			if (isCreate) {
				setSmscDefault(path);
				props.put("FLAG", "100");
				props.put("RR", "100");
			}
		} catch (IOException e) {
			logger.error(path, e.fillInStackTrace());
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

	public static void setSmscDefault(String path) {
		setContent(path, "FLAG = 100\nRR = no");
	}

	public static void setSmscBlocked(String path) {
		setContent(path, "FLAG = 404\nRR = no");
	}

	public static void setDefaultFlag(String path) {
		setContent(path, "FLAG = " + FlagStatus.DEFAULT);
	}

	public static void setRefreshFlag(String path) {
		setContent(path, "FLAG = " + FlagStatus.REFRESH);
	}

	public static void setFlag(String path, String flag) {
		setContent(path, "FLAG = " + flag);
	}

	public static void setBlocked(String path) {
		setContent(path, "FLAG = " + FlagStatus.BLOCKED);
	}

	public static boolean setContent(String path, String flag) {
		FileOutputStream fileOutputStream = null;
		boolean done = false;
		try {
			fileOutputStream = new FileOutputStream(path);
			fileOutputStream.write(flag.getBytes());
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
}
