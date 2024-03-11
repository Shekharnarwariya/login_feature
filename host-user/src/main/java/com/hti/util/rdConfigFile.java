/**
 * Class that reads Configuration from a specified file
 *
 * @version 1.0, 02/12/06
 */
package com.hti.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class rdConfigFile {
	public rdConfigFile() {
	}

	// ************************Added by Amit_vish date 10-JAN-2012 **************
	public String getValueSmsc(String file_param, String arg_param) {
		String read_value = "ERROR";
		boolean createNew = false, readDefault = false;
		Properties properties = null;
		BufferedInputStream bufferedInputStream = null;
		FileOutputStream outputStream = null;
		long fileSize = 0;
		File file = null;
		try {
			file = new File(file_param);
			if (file.exists()) {
				fileSize = file.length();
				if (fileSize > 0) {
					try {
						bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
						properties = new Properties();
						properties.load(bufferedInputStream);
						read_value = properties.getProperty(arg_param);
					} catch (IOException ioe) {
						System.err.println("Exception ::" + ioe);
						readDefault = true;
					} finally {
						if (properties != null) {
							properties.clear();
							properties = null;
						}
						if (bufferedInputStream != null) {
							bufferedInputStream.close();
							bufferedInputStream = null;
						}
					}
				} else {
					createNew = true;
				}
			} else {
				createNew = true;
			}
			if (createNew) {
				try {
					readDefault = true;
					file.createNewFile();
					System.out.println("File Created Successfully < " + file + " >");
					outputStream = new FileOutputStream(file);
					byte[] buff = "FLAG = 100\nRR = no\nUSER = no\nDB=no\nPrefix=no".getBytes();
					outputStream.write(buff);
					buff = null;
				} catch (IOException ioe) {
					System.out.println("IOException:getValueSmsc(2)::" + ioe);
				} finally {
					if (outputStream != null) {
						outputStream.flush();
						outputStream.close();
					}
				}
			}
			if (readDefault) {
				if (arg_param.equalsIgnoreCase("RR")) {
					read_value = "no";
				}
				if (arg_param.equalsIgnoreCase("USER")) {
					read_value = "no";
				}
				if (arg_param.equalsIgnoreCase("Prefix")) {
					read_value = "no";
				}
				if (arg_param.equalsIgnoreCase("DB")) {
					read_value = "no";
				}
				if (arg_param.equalsIgnoreCase("FLAG")) {
					read_value = "100";
				}
			}
		} catch (IOException ioe) {
			System.out.println("IOException:getValueSmsc(3)::" + ioe);
		} catch (Exception ex) {
			System.out.println("Exception:getValueSmsc(3)::" + ex);
		}
		// System.out.println("1 >> " + arg_param + " = " + read_value);
		return read_value;
	}
	// ************************ End ********************************************

	public void setValue(String filename, String keys, String value) {
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String fullfile = "";
			while ((strLine = br.readLine()) != null) {
				if (strLine.startsWith(keys)) {
					String flagValue = (strLine.substring(strLine.indexOf("=") + 1, strLine.length())).trim();
					if (flagValue.equalsIgnoreCase("TRUE")) {
						String value1 = keys + " = " + value;
						fullfile = fullfile + value1 + "\n";
					}
				} else {
					fullfile = fullfile + strLine + "\n";
				}
			}
			FileWriter wstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(wstream);
			out.write(fullfile);
			out.close();
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	// public String getValueConfigFile(String file_param, String arg_param) {}

	/*
	 * public String getFlagValue(String file_param, String arg_param) { Exception exception = null; Properties rcf_rcf_props = new Properties(); String read_value = null; BufferedInputStream rcf_bis = null; try { File file = new File(file_param); if (file.exists()) { rcf_bis = new
	 * BufferedInputStream(new FileInputStream(file)); rcf_rcf_props.load(rcf_bis); read_value = rcf_rcf_props.getProperty(arg_param); } if (read_value == null) { /* try { rcf_bis.close(); rcf_bis = null; rcf_rcf_props.clear(); rcf_rcf_props = null; } catch (IOException rcf_IOE) { read_value =
	 * "ERROR :: " + rcf_IOE.toString(); exception = rcf_IOE; }
	 */
	/*
	 * System.err.println("----< " + file_param + " >----File Created successfully "); try { java.io.FileOutputStream file_output = new java.io.FileOutputStream(file_param); byte[] buff = "FLAG = 100".getBytes(); file_output.write(buff); file_output.close(); buff = null; read_value = "100"; } catch
	 * (Exception ex) { System.err.println("Error: " + ex.getMessage()); } } } catch (Exception rcf_EXC) { read_value = "ERROR :: " + rcf_EXC.toString(); exception = rcf_EXC; } finally { try { if (rcf_bis != null) { rcf_bis.close(); rcf_bis = null; rcf_rcf_props.clear(); rcf_rcf_props = null; } }
	 * catch (IOException rcf_FIOE) { exception = rcf_FIOE; } catch (Exception rcf_FEXC) { exception = rcf_FEXC; }
	 * 
	 * } return read_value; }
	 */
	public String getCheckInitValue(String file_param, String arg_param) {
		Exception exception = null;
		Properties rcf_rcf_props = new Properties();
		String read_value = null;
		BufferedInputStream rcf_bis = null;
		try {
			rcf_bis = new BufferedInputStream(new FileInputStream(new File(file_param)));
			rcf_rcf_props.load(rcf_bis);
			read_value = rcf_rcf_props.getProperty(arg_param);
			try {
				rcf_bis.close();
				rcf_bis = null;
				rcf_rcf_props.clear();
				rcf_rcf_props = null;
			} catch (IOException rcf_IOE) {
				read_value = "ERROR :: " + rcf_IOE.toString();
				exception = rcf_IOE;
			}
		} catch (Exception rcf_EXC) {
			read_value = "ERROR :: " + rcf_EXC.toString();
			exception = rcf_EXC;
		} finally {
			try {
				if (rcf_bis != null) {
					rcf_bis.close();
					rcf_bis = null;
					rcf_rcf_props.clear();
					rcf_rcf_props = null;
				}
			} catch (IOException rcf_FIOE) {
				exception = rcf_FIOE;
			} catch (Exception rcf_FEXC) {
				exception = rcf_FEXC;
			}
			if (exception != null) {
				System.err.println("----< " + file_param + " >----File Created successfully ");
				try {
					java.io.FileOutputStream file_output = new java.io.FileOutputStream(file_param);
					byte[] buff = "MBSN =   \nHDSN = ".getBytes();
					file_output.write(buff);
					file_output.close();
					buff = null;
				} catch (Exception ex) {
					System.err.println("Error: " + ex.getMessage());
				}
			}
		}
		return read_value;
	}

	public String getValueMailFile(String file_param, String arg_param) {
		Exception exception = null;
		Properties rcf_rcf_props = new Properties();
		String read_value = null;
		BufferedInputStream rcf_bis = null;
		try {
			rcf_bis = new BufferedInputStream(new FileInputStream(new File(file_param)));
			rcf_rcf_props.load(rcf_bis);
			read_value = rcf_rcf_props.getProperty(arg_param);
			try {
				rcf_bis.close();
				rcf_bis = null;
				rcf_rcf_props.clear();
				rcf_rcf_props = null;
			} catch (IOException rcf_IOE) {
				read_value = "ERROR :: " + rcf_IOE.toString();
				exception = rcf_IOE;
			}
		} catch (Exception rcf_EXC) {
			read_value = "ERROR :: " + rcf_EXC.toString();
			exception = rcf_EXC;
		} finally {
			try {
				if (rcf_bis != null) {
					rcf_bis.close();
					rcf_bis = null;
					rcf_rcf_props.clear();
					rcf_rcf_props = null;
				}
			} catch (IOException rcf_FIOE) {
				exception = rcf_FIOE;
			} catch (Exception rcf_FEXC) {
				exception = rcf_FEXC;
			}
			if (exception != null) {
				read_value = "ERROR :: " + exception.toString();
			}
		}
		return read_value;
	}
}
