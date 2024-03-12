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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 */
public class FileUtil {
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

	public static String readSmscFlag(String smsc_name) {
		String flagValue = FlagStatus.DEFAULT;
		try {
			flagValue = readValue(Constants.SMSC_DIR + smsc_name + ".txt", "FLAG");
		} catch (FileNotFoundException fnfex) {
			logger.info("Smsc Flag File Not Found :" + smsc_name);
			setSmscDefault(smsc_name);
		} catch (IOException ioe) {
			logger.info("Smsc Flag File" + smsc_name + ": " + ioe);
		}
		return flagValue;
	}

	public static String readSmscParam(String smsc_name, String param) {
		String param_value = null;
		try {
			param_value = readValue(Constants.SMSC_DIR + smsc_name + ".txt", param);
		} catch (FileNotFoundException fnfex) {
			logger.info("Smsc Flag File Not Found :" + smsc_name);
			setSmscDefault(smsc_name);
		} catch (IOException ioe) {
			logger.info("Smsc Flag File" + smsc_name + ": " + ioe);
		}
		return param_value;
	}

	public static void setSmscDefault(String smsc_name) {
		setDefault(Constants.SMSC_DIR + smsc_name + ".txt", "FLAG = 100\nRR = no\nUSER = no\nDB = no\nPrefix = no");
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

	private static String readValue(String path, String param) throws FileNotFoundException, IOException {
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

	public static void setDefaultFlag(String path) {
		setDefault(path, "FLAG = " + FlagStatus.DEFAULT);
	}

	public static String readFlag(String path, boolean isCreate) throws IOException {
		String flagValue = null;
		try {
			flagValue = readValue(path, "FLAG");
		} catch (FileNotFoundException fnfex) {
			logger.info("File Not Found :" + path);
			if (isCreate) {
				logger.info("Creating File :" + path);
				setDefaultFlag(path);
				flagValue = FlagStatus.DEFAULT;
			}
		}
		return flagValue;
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

	public static Object readObject(String filename, boolean delete) throws FileNotFoundException, IOException {
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(filename));
			obj = ois.readObject();
		} catch (ClassNotFoundException ex) { // Ignore
		} finally {
			if (ois != null) {
				try {
					ois.close();
					if (delete) {
						File file = new File(filename);
						if (file.exists()) {
							if (file.delete()) {
								logger.info("Backup file Deleted -> " + file);
							} else {
								logger.info("Unable to Delete Backup file -> " + file);
							}
						}
					}
				} catch (IOException ioe) {
				}
			}
		}
		return obj;
	}

	public static void writeObject(String filename, Object obj) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		if (obj != null) {
			try {
				objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(filename)));
				objectOutputStream.writeObject(obj);
			} finally {
				if (objectOutputStream != null) {
					try {
						objectOutputStream.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
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
}
