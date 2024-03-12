package com.hti.tw.filter;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedHashMap;

import com.hti.util.GlobalCache;

public class FilterService {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Map<Long, Map<String, Boolean>> rules = Collections
			.synchronizedMap(new HashMap<Long, Map<String, Boolean>>());

	public static synchronized void load2wayFilter() {
		rules.clear();
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		Map<Long, Map<String, Boolean>> temp_rules = new HashMap<Long, Map<String, Boolean>>();
		String sql = "select A.number,A.type,A.sender,B.sources,B.suffix from 2way_bsfm A,2way_keyword B where A.keyword_id = B.id order by A.receivedOn DESC;";
		try {
			con = GlobalCache.connnection_pool_1.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				long number = rs.getLong("A.number");
				boolean type = false;
				if (rs.getString("A.type").equalsIgnoreCase("start")) {
					type = true;
				}
				String sender = rs.getString("A.sender");
				String sources = rs.getString("B.sources");
				String suffix = rs.getString("B.suffix");
				//logger.info(type + "," + number + "," + sender + "," + sources);
				if (number == 0) {
					logger.info("Invalid 2wayFilter Entry: " + number + "," + sender + "," + sources);
					continue;
				}
				if (sender == null || sender.length() == 0) {
					logger.info("Invalid 2wayFilter Entry: " + number + "," + sender + "," + sources);
					continue;
				}
				Map<String, Boolean> source_map = null;
				if (temp_rules.containsKey(number)) {
					source_map = temp_rules.get(number);
				} else {
					source_map = new LinkedHashMap<String, Boolean>();
				}
				if (sender.equalsIgnoreCase(suffix) && sources != null && sources.length() > 0) {
					source_map.put(sources, type);
				} else {
					source_map.put(sender, type);
				}
				temp_rules.put(number, source_map);
			}
		} catch (Exception e) {
			logger.error("load2wayFilter()", e.fillInStackTrace());
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
		if (!temp_rules.isEmpty()) {
			rules.putAll(temp_rules);
			System.out.println("*******************************");
			logger.info("2wayFilter Rules: " + rules);
			System.out.println("*******************************");
		}
	}

	public static synchronized boolean filter(String number_str, String source) {
		if (!rules.isEmpty()) {
			try {
				long number = Long.parseLong(number_str);
				if (rules.containsKey(number)) {
					//System.out.println(source + "-> " + rules.get(number));
					for (String source_key : rules.get(number).keySet()) {
						for (String regex_token : source_key.split(",")) {
							if (regex_token != null && regex_token.length() > 0) {
								//System.out.println(regex_token + " matching: " + source);
								if (Pattern.compile("(?i)" +regex_token).matcher(source).find()) {
									//System.out.println(regex_token + " matched: " + source);
									return rules.get(number).get(source_key);
								}
							}
						}
					}
				}
			} catch (NumberFormatException e) {
				logger.info("Invalid Number: " + number_str);
			}
		}
		return true;
	}
}
