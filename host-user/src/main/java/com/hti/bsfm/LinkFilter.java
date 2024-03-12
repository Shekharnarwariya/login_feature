package com.hti.bsfm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.util.GlobalCache;

public class LinkFilter {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");

	public static synchronized void loadLinks() {
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		String sql = "select * from url_check";
		Map<String, Boolean> map = new java.util.HashMap<String, Boolean>();
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				String url = rs.getString("url");
				String result = rs.getString("clean");
				if (result != null && result.length() > 0) {
					if (result.toLowerCase().contains("Malicious: No".toLowerCase())) {
						map.put(url, false);
					} else {
						map.put(url, true);
					}
				}
			}
		} catch (Exception e) {
			logger.error("loadLinks()", e.fillInStackTrace());
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
			GlobalCache.connection_pool_proc.putConnection(con);
		}
		if (!map.isEmpty()) {
			GlobalCache.ContentWebLinks.putAll(map);
		}
		logger.info("Cached Web Links: " + GlobalCache.ContentWebLinks.size());
	}

	public static synchronized void insertLink(String link) {
		Connection con = null;
		Statement statement = null;
		String sql = "insert ignore into url_check(url) values('" + link + "')";
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			statement.execute(sql);
		} catch (Exception e) {
			logger.error("insertLink:" + link, e.fillInStackTrace());
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			GlobalCache.connection_pool_proc.putConnection(con);
		}
	}

	public static synchronized boolean isMaliciousLink(String link) {
		Connection con = null;
		Statement statement = null;
		ResultSet rs = null;
		boolean isMalicious = false;
		String sql = "select clean from url_check where url='" + link + "'";
		try {
			con = GlobalCache.connection_pool_proc.getConnection();
			statement = con.createStatement();
			rs = statement.executeQuery(sql);
			if (rs.next()) {
				String result = rs.getString("clean");
				if (result != null && result.length() > 0) {
					if (result.toLowerCase().contains("Malicious: No".toLowerCase())) {
						GlobalCache.ContentWebLinks.put(link, false);
					} else {
						GlobalCache.ContentWebLinks.put(link, true);
						isMalicious = true;
					}
				}
			}
		} catch (Exception e) {
			logger.error("loadLinks()", e.fillInStackTrace());
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
			GlobalCache.connection_pool_proc.putConnection(con);
		}
		return isMalicious;
	}
}
