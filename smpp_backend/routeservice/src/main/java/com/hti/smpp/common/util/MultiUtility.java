package com.hti.smpp.common.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;

public class MultiUtility {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(MultiUtility.class);

	public static boolean refreshRouting(int userId) {
		String systemId = GlobalVars.UserEntries.get(userId).getSystemId();
		String path = Constants.USER_FLAG_DIR + systemId + ".txt";
		String flagVal = readFlag(path);
		if ((flagVal != null) && (!flagVal.trim().equalsIgnoreCase("404"))) {
			changeFlag(path, "707");
		}
		return true;
	}

	public static boolean changeFlag(String path, String flagValue) {
		String writeData = "FLAG = " + flagValue;
		RandomAccessFile randomAccessFile = null;
		boolean done = false;
		try {
			randomAccessFile = new RandomAccessFile(path, "rw");
			randomAccessFile.seek(0);
			randomAccessFile.writeBytes(writeData);
			done = true;
			System.out.println("[" + path + "] Flag Setted to : " + flagValue);
		} catch (Exception ex) {
			System.out.println("Exception :changeFlag()::" + ex);
		} finally {
			try {
				if (randomAccessFile != null) {
					randomAccessFile.close();
				}
			} catch (IOException ioe) {
			}
		}
		return done;
	}

	public static String readFlag(String path) {
		String flagValue = FlagStatus.DEFAULT;
		// FileInputStream fileInputStream = null;
		// DataInputStream dataInputStream = null;
		BufferedReader bufferedReader = null;
		String lines = "";
		try {
			// fileInputStream = new FileInputStream(path);
			// dataInputStream = new DataInputStream(fileInputStream);
			bufferedReader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(path))));
			while ((lines = bufferedReader.readLine()) != null) {
				if (lines.contains("FLAG")) {
					flagValue = (lines.substring(lines.indexOf("=") + 1, lines.length())).trim();
					break;
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception :readFlag()::" + ex);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
				}
				bufferedReader = null;
			}
		}
		return flagValue;
	}

	public static String readContent(String filename) throws FileNotFoundException, IOException {
		String content = "";
		content = readStream(new FileInputStream(filename));
		return content;
	}

	public static boolean writeHttpLog(String content) {
		PrintStream printStream = null;
		String filename = IConstants.WEBAPP_DIR + "log//http_log.txt";
		boolean done = false;
		try {
			calculateFileSize(filename, 5000);
			printStream = new PrintStream(new FileOutputStream(filename, true), true, "UTF-8");
			// content = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + "
			// [ " + content + " ]";
			printStream.println(content);
			done = true;
		} catch (IOException ioe) {
			System.out.println("Error(writeContent): " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	public static boolean writeXmlLog(String content) {
		PrintStream printStream = null;
		String filename = IConstants.WEBAPP_DIR + "log//xml_log.txt";
		boolean done = false;
		try {
			calculateFileSize(filename, 5000);
			printStream = new PrintStream(new FileOutputStream(filename, true), true, "UTF-8");
			// content = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + "
			// [ " + content + " ]";
			printStream.println(content);
			done = true;
		} catch (IOException ioe) {
			System.out.println("Error(writeContent): " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	public static boolean writeContent(String filename, String content, boolean append) {
		PrintStream printStream = null;
		boolean done = false;
		try {
			calculateFileSize(filename, 5000);
			printStream = new PrintStream(new FileOutputStream(filename, append), true, "UTF-8");
			content = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + " [ " + content + " ]";
			printStream.println(content);
			done = true;
		} catch (IOException ioe) {
			System.out.println("Error(writeContent): " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	public static void writeExcludeNumbers(String systemId, String content) throws Exception {
		PrintStream printStream = null;
		File dir = new File(IConstants.BIN_DIR + "numbers//exclude//");
		if (!dir.exists()) {
			if (dir.mkdir()) {
				logger.info(systemId + " Exclude Numbers Dir Created");
			} else {
				logger.error(systemId + " Exclude Numbers Dir Creation Failed");
			}
		}
		File numberfile = new File(IConstants.BIN_DIR + "numbers//exclude//" + systemId + ".txt");
		if (!numberfile.exists()) {
			try {
				if (numberfile.createNewFile()) {
					logger.info(systemId + " Exclude Numbers file Created:" + numberfile.getPath());
				} else {
					logger.info(systemId + " Exclude Numbers file Creation failed: " + numberfile.getPath());
				}
			} catch (IOException e) {
				logger.info(systemId + " Exclude Numbers file Creation IOError");
				throw new IOException();
			}
		}
		try {
			printStream = new PrintStream(new FileOutputStream(numberfile), false);
			printStream.print(content);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
	}

	public static String readExcludeNumbers(String systemId) throws Exception {
		String content = null;
		File dir = new File(IConstants.BIN_DIR + "numbers//exclude//");
		if (dir.exists()) {
			File numberfile = new File(IConstants.BIN_DIR + "numbers//exclude//" + systemId + ".txt");
			if (numberfile.exists()) {
				content = readContent(IConstants.BIN_DIR + "numbers//exclude//" + systemId + ".txt");
			} else {
				logger.info(systemId + " Exclude Numbers File Not Exist");
			}
		} else {
			logger.info(systemId + " Exclude Numbers Dir Not Exist");
		}
		return content;
	}

	public static boolean removeExcludeNumbers(String systemId) throws Exception {
		File dir = new File(IConstants.BIN_DIR + "numbers//exclude//");
		if (dir.exists()) {
			File numberfile = new File(IConstants.BIN_DIR + "numbers//exclude//" + systemId + ".txt");
			if (numberfile.exists()) {
				return numberfile.delete();
			} else {
				logger.info(systemId + " Exclude Numbers File Not Exist");
			}
		} else {
			logger.info(systemId + " Exclude Numbers Dir Not Exist");
		}
		return false;
	}

	public static boolean writeNumbers(String filename, String content) {
		PrintStream printStream = null;
		boolean done = false;
		try {
			printStream = new PrintStream(new FileOutputStream(filename), true);
			printStream.print(content);
			done = true;
		} catch (IOException ioe) {
			System.out.println("Error(writeNumbers): " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	public static boolean writeMailContent(String filename, String content) {
		PrintStream printStream = null;
		boolean done = false;
		try {
			calculateFileSize(filename, 5000);
			printStream = new PrintStream(new FileOutputStream(filename, false), true, "UTF-8");
			printStream.println(content);
			done = true;
		} catch (IOException ioe) {
			System.out.println("Error(writeMailContent): " + ioe);
		} finally {
			if (printStream != null) {
				printStream.close();
			}
		}
		return done;
	}

	public static String readStream(InputStream inputStream) throws FileNotFoundException, IOException {
		String content = "", lines = "";
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			while ((lines = bufferedReader.readLine()) != null) {
				content += lines + "\n";
			}
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ioe) {
					// nothing
				}
				bufferedReader = null;
			}
		}
		return content;
	}

	public static List<String> readLines(String file) throws FileNotFoundException, IOException {
		List<String> list = new java.util.ArrayList<String>();
		String lines = "";
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			while ((lines = bufferedReader.readLine()) != null) {
				list.add(lines);
			}
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ioe) {
					// nothing
				}
				bufferedReader = null;
			}
		}
		return list;
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

	public static Object readObject(String filename, boolean delete) throws FileNotFoundException, IOException {
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(filename));
			obj = ois.readObject();
		} catch (ClassNotFoundException ex) {
			// Ignore
		} finally {
			if (ois != null) {
				try {
					ois.close();
					if (delete) {
						File file = new File(filename);
						if (file.exists()) {
							if (file.delete()) {
								System.out.println("Backup file Deleted -> " + file);
							} else {
								System.out.println("Unable to Delete Backup file -> " + file);
							}
						}
					}
				} catch (IOException ioe) {
				}
			}
		}
		return obj;
	}

	public static Date getNextDay(Date date, int add_day) {
		Date next_time = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, add_day);
		next_time = calendar.getTime();
		System.out.println("Given Date : " + formatDate(date));
		System.out.println("Next Date : " + formatDate(next_time));
		return next_time;
	}

	public static Date getNextMonth(Date date, int add_month) {
		Date next_time = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH, add_month);
		next_time = calendar.getTime();
		System.out.println("Given Date : " + formatDate(date));
		System.out.println("Next Date : " + formatDate(next_time));
		return next_time;
	}

	public static Date getNextYear(Date date, int add_year) {
		Date next_time = null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, add_year);
		next_time = calendar.getTime();
		System.out.println("Given Date : " + formatDate(date));
		System.out.println("Next Date : " + formatDate(next_time));
		return next_time;
	}

	private static String formatDate(Date date) {
		String form_date = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		form_date = df.format(date);
		return form_date;
	}

	private static void calculateFileSize(String filename, int limit) throws IOException {
		long fileSize = 0;
		File file = null;
		boolean renamed = false, created = false;
		file = new File(filename);
		if (!file.isFile() || !file.exists()) {
			created = file.createNewFile();
			if (created) {
				System.out.println("File Created :: " + filename);
			} else {
				System.out.println("Error in File Creation :: " + filename);
			}
		} else {
			fileSize = file.length();
			fileSize = fileSize / 1024;
			if (fileSize >= limit) {
				System.out.println("FILE SIZE(" + filename + ")::" + fileSize + "KB");
				String extension = "", newFileName = "";
				String currentDate = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(new Date());
				extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
				newFileName = filename.substring(0, filename.lastIndexOf("."));
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
