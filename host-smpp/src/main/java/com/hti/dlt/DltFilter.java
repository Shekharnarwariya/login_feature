package com.hti.dlt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import com.hti.util.FixedLengthMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.Converter;
import com.hti.util.GlobalCache;

public class DltFilter {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Logger tracklogger = LoggerFactory.getLogger("trackLogger");
	private static Map<Integer, String> hex_mapping = new HashMap<Integer, String>();
	private static Map<String, String> SenderMapping = Collections.synchronizedMap(new HashMap<String, String>());
	private static Map<String, Map<String, String>> TemplateMapping = Collections
			.synchronizedMap(new HashMap<String, Map<String, String>>());
	private static Map<String, FixedLengthMap<String, String>> RecentTemplateMapping = Collections
			.synchronizedMap(new HashMap<String, FixedLengthMap<String, String>>());
	private static Map<String, List<String>> MissingTemplateMapping = Collections
			.synchronizedMap(new HashMap<String, List<String>>());
	private static String DEFAULT_PE_ID = null;
	static {
		hex_mapping.put(65, "41");
		hex_mapping.put(66, "42");
		hex_mapping.put(67, "43");
		hex_mapping.put(68, "44");
		hex_mapping.put(69, "45");
		hex_mapping.put(70, "46");
		hex_mapping.put(71, "47");
		hex_mapping.put(72, "48");
		hex_mapping.put(73, "49");
		hex_mapping.put(74, "4A");
		hex_mapping.put(75, "4B");
		hex_mapping.put(76, "4C");
		hex_mapping.put(77, "4D");
		hex_mapping.put(78, "4E");
		hex_mapping.put(79, "4F");
		hex_mapping.put(80, "50");
		hex_mapping.put(81, "51");
		hex_mapping.put(82, "52");
		hex_mapping.put(83, "53");
		hex_mapping.put(84, "54");
		hex_mapping.put(85, "55");
		hex_mapping.put(86, "56");
		hex_mapping.put(87, "57");
		hex_mapping.put(88, "58");
		hex_mapping.put(89, "59");
		hex_mapping.put(90, "5A");
		hex_mapping.put(97, "61");
		hex_mapping.put(98, "62");
		hex_mapping.put(99, "63");
		hex_mapping.put(100, "64");
		hex_mapping.put(101, "65");
		hex_mapping.put(102, "66");
		hex_mapping.put(103, "67");
		hex_mapping.put(104, "68");
		hex_mapping.put(105, "69");
		hex_mapping.put(106, "6A");
		hex_mapping.put(107, "6B");
		hex_mapping.put(108, "6C");
		hex_mapping.put(109, "6D");
		hex_mapping.put(110, "6E");
		hex_mapping.put(111, "6F");
		hex_mapping.put(112, "70");
		hex_mapping.put(113, "71");
		hex_mapping.put(114, "72");
		hex_mapping.put(115, "73");
		hex_mapping.put(116, "74");
		hex_mapping.put(117, "75");
		hex_mapping.put(118, "76");
		hex_mapping.put(119, "77");
		hex_mapping.put(120, "78");
		hex_mapping.put(121, "79");
		hex_mapping.put(122, "7A");
		hex_mapping.put(48, "30");
		hex_mapping.put(49, "31");
		hex_mapping.put(50, "32");
		hex_mapping.put(51, "33");
		hex_mapping.put(52, "34");
		hex_mapping.put(53, "35");
		hex_mapping.put(54, "36");
		hex_mapping.put(55, "37");
		hex_mapping.put(56, "38");
		hex_mapping.put(57, "39");
		hex_mapping.put(27, "1B");
		hex_mapping.put(12, "1B0A");
		hex_mapping.put(94, "1B14");
		hex_mapping.put(123, "1B28");
		hex_mapping.put(125, "1B29");
		hex_mapping.put(92, "1B2F");
		hex_mapping.put(91, "1B3C");
		hex_mapping.put(126, "1B3D");
		hex_mapping.put(93, "1B3E");
		hex_mapping.put(124, "1B40");
		hex_mapping.put(8364, "1B65");
		hex_mapping.put(37, "25");
		hex_mapping.put(38, "26");
		hex_mapping.put(39, "27");
		hex_mapping.put(40, "28");
		hex_mapping.put(41, "29");
		hex_mapping.put(42, "2A");
		hex_mapping.put(43, "2B");
		hex_mapping.put(44, "2C");
		hex_mapping.put(45, "2D");
		hex_mapping.put(46, "2E");
		hex_mapping.put(47, "2F");
		hex_mapping.put(58, "3A");
		hex_mapping.put(59, "3B");
		hex_mapping.put(60, "3C");
		hex_mapping.put(61, "3D");
		hex_mapping.put(62, "3E");
		hex_mapping.put(63, "3F");
		hex_mapping.put(161, "40");//
		hex_mapping.put(228, "7B");//
		hex_mapping.put(246, "7C");//
		hex_mapping.put(241, "7D");//
		hex_mapping.put(252, "7E");//
		hex_mapping.put(224, "7F");//
		hex_mapping.put(196, "5B");//
		hex_mapping.put(214, "5C");//
		hex_mapping.put(209, "5D");//
		hex_mapping.put(220, "5E");//
		hex_mapping.put(167, "5F");//
		hex_mapping.put(191, "60");
		hex_mapping.put(64, "00"); // @
		hex_mapping.put(163, "01");//
		hex_mapping.put(36, "02");//
		hex_mapping.put(165, "03");
		hex_mapping.put(232, "04");
		hex_mapping.put(233, "05");
		hex_mapping.put(249, "06");
		hex_mapping.put(236, "07");
		hex_mapping.put(242, "08");
		hex_mapping.put(199, "09");
		hex_mapping.put(216, "0B");
		hex_mapping.put(248, "0C");
		hex_mapping.put(13, "0D");
		hex_mapping.put(197, "0E");
		hex_mapping.put(229, "0F");
		hex_mapping.put(95, "11");
		hex_mapping.put(198, "1C");
		hex_mapping.put(230, "1D");
		hex_mapping.put(223, "1E");
		hex_mapping.put(201, "1F");
		hex_mapping.put(10, "0A");
		hex_mapping.put(20, "32");
		hex_mapping.put(916, "10");
		hex_mapping.put(934, "12");
		hex_mapping.put(915, "13");// Correct Encoding
		hex_mapping.put(923, "14");
		hex_mapping.put(937, "15");// Correct Encoding
		hex_mapping.put(928, "16");// Correct Encoding
		hex_mapping.put(936, "17");// Correct Encoding
		hex_mapping.put(931, "18");
		hex_mapping.put(920, "19");
		hex_mapping.put(926, "1A");
		hex_mapping.put(33, "21");
		hex_mapping.put(34, "22");
		hex_mapping.put(35, "23");
		hex_mapping.put(164, "24");
		hex_mapping.put(32, "20");
	}

	public static synchronized void loadRules() {
		// --------- checking for senderId mappings -------------
		logger.info("<-- Initializing Dlt Configuration -->");
		Map<String, String> tempSenderMapping = new HashMap<String, String>();
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select pe_id,sender from dlt_config";
		// String temp_content = null;
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String source = rs.getString("sender");
				String peId = rs.getString("pe_id");
				if (source != null) {
					if (source.trim().length() > 0) {
						source = source.toLowerCase();
					} else {
						source = null;
					}
				}
				if (peId == null || peId.length() == 0) {
					logger.error(source + " <-- Invalid Dlt PE_ID Configured --> ");
					continue;
				}
				if (source == null) {
					DEFAULT_PE_ID = peId;
				} else {
					tempSenderMapping.put(source.toLowerCase(), peId);
				}
			}
			SenderMapping.clear();
			SenderMapping.putAll(tempSenderMapping);
			System.out.println("*******************************************");
			logger.info("Dlt Active Senders:" + SenderMapping.size());
			System.out.println("*******************************************");
		} catch (Exception e) {
			logger.error("loadProfiles()", e.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
		// --------- checking for content mappings -------------
		Map<String, Map<String, String>> peid_template = new HashMap<String, Map<String, String>>();
		Map<String, String> temp_id_mapping = null;
		sql = "select pe_id,template,temp_id from dlt_templ";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String pe_id = rs.getString("pe_id");
				String temp_content = rs.getString("template");
				String temp_Id = rs.getString("temp_id");
				String content = "";
				if (temp_content != null && temp_content.length() > 0) {
					if (temp_content.trim().length() > 0) {
						String content_token = Converter.getUnicode(temp_content.toCharArray());
						if (content_token.matches(
								"^[A-Za-z0-9 \\r\\n@£$¥èéùìòÇØøÅå\u0394_\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u00A4\u03A3\u0398\u039EÆæßÉ!\"#$%&'()*+,\\-./:;<=>?¡ÄÖÑÜ§¿äöñüà^{}\\\\\\[~\\]|\u20AC]*$")) {
							String hex_token = "";
							for (char c : content_token.toCharArray()) {
								if (hex_mapping.containsKey((int) c)) {
									hex_token += hex_mapping.get((int) c);
								} else {
									hex_token += (int) c;
								}
							}
							content_token = getContent(hex_token.toCharArray());
						}
						if (content_token.length() > 0) {
							content = content_token;
						} else {
							content = null;
						}
					} else {
						content = null;
					}
				}
				if (temp_Id == null || temp_Id.length() == 0) {
					logger.error("dlt_" + pe_id + "[" + temp_content + "] <-- Invalid Dlt Template_ID Configured --> ");
					continue;
				}
				if (content == null) {
					logger.error("dlt_" + pe_id + " <-- Invalid Dlt Template Configured --> ");
					continue;
				}
				if (peid_template.containsKey(pe_id)) {
					temp_id_mapping = peid_template.get(pe_id);
				} else {
					temp_id_mapping = new HashMap<String, String>();
				}
				temp_id_mapping.put(content.toLowerCase(), temp_Id);
				peid_template.put(pe_id, temp_id_mapping);
			}
		} catch (Exception e) {
			logger.error("", e.fillInStackTrace());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connnection_pool_1.putConnection(con);
		}
		TemplateMapping.clear();
		TemplateMapping.putAll(peid_template);
		RecentTemplateMapping.clear();
		MissingTemplateMapping.clear();
		System.out.println("*******************************************");
		logger.info("Dlt Active Templates:" + TemplateMapping.size());
		System.out.println("*******************************************");
	}

	public static String findPeID(String sender) {
		try {
			if (SenderMapping.containsKey(sender)) {
				return SenderMapping.get(sender);
			}
		} catch (Exception e) {
			logger.error(sender, e.getMessage());
		}
		return DEFAULT_PE_ID;
	}

	public static String findTemplateID(String PE_ID, String content) {
		try {
			// check from missing cache
			if (MissingTemplateMapping.containsKey(PE_ID)) {
				if (MissingTemplateMapping.get(PE_ID).contains(content.toLowerCase())) {
					tracklogger.debug(PE_ID + "[" + content + "] Missing Content.");
					return null;
				}
			}
			// check from recent cache
			if (RecentTemplateMapping.containsKey(PE_ID)) {
				for (String entry_content : RecentTemplateMapping.get(PE_ID).keySet()) {
					String filter = entry_content.replaceAll("[()+$^*?{}\\[\\]]", " ").replaceAll("#variable#",
							"[\\\\p{L}\\\\u0000-\\\\u00FF]*");
					if (Pattern.matches("(?i)" + filter, content.replaceAll("[()+$^*?{}\\[\\]]", " "))) {
						return RecentTemplateMapping.get(PE_ID).get(entry_content);
					}
				}
			}
			tracklogger.debug(PE_ID + "[" + content + "] Recent Not Found.");
			// check from main cache
			if (TemplateMapping.containsKey(PE_ID)) {
				for (String entry_content : TemplateMapping.get(PE_ID).keySet()) {
					String filter = entry_content.replaceAll("[()+$^*?{}\\[\\]]", " ").replaceAll("#variable#",
							"[\\\\p{L}\\\\u0000-\\\\u00FF]*");
					if (Pattern.matches("(?i)" + filter, content.replaceAll("[()+$^*?{}\\[\\]]", " "))) {
						if (RecentTemplateMapping.containsKey(PE_ID)) {
							RecentTemplateMapping.get(PE_ID).put(entry_content,
									TemplateMapping.get(PE_ID).get(entry_content));
						} else {
							FixedLengthMap<String, String> recentTemplates = new FixedLengthMap<String, String>(20);
							recentTemplates.put(entry_content, TemplateMapping.get(PE_ID).get(entry_content));
							RecentTemplateMapping.put(PE_ID, recentTemplates);
						}
						return TemplateMapping.get(PE_ID).get(entry_content);
					}
				}
			}
		} catch (Exception e) {
			logger.error(PE_ID, e.getMessage());
		}
		try {
			if (MissingTemplateMapping.containsKey(PE_ID)) {
				if (!MissingTemplateMapping.get(PE_ID).contains(content.toLowerCase())) {
					if (MissingTemplateMapping.get(PE_ID).size() >= 100) {
						MissingTemplateMapping.get(PE_ID).remove(0);
					}
					MissingTemplateMapping.get(PE_ID).add(content.toLowerCase());
				}
			} else {
				List<String> missing_list = new java.util.ArrayList<String>();
				missing_list.add(content.toLowerCase());
				MissingTemplateMapping.put(PE_ID, missing_list);
			}
		} catch (Exception e) {
			logger.error(PE_ID, e.getMessage());
		}
		return null;
	}

	private static String getContent(char[] buffer) {
		String unicode = "";
		int code = 0;
		int j = 0;
		char[] unibuffer = new char[buffer.length / 2];
		try {
			for (int i = 0; i < buffer.length; i += 2) {
				code += Character.digit(buffer[i], 16) * 16;
				code += Character.digit(buffer[i + 1], 16);
				unibuffer[j++] = (char) code;
				code = 0;
			}
			unicode = new String(unibuffer);
		} catch (Exception e) {
			logger.info("getContent(): " + e);
		}
		return unicode;
	}
}
