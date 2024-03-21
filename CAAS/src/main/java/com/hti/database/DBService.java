/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.user.dto.BalanceEntry;
import com.hti.util.GlobalVar;
import java.sql.PreparedStatement;
import com.hti.util.PasswordConverter;

/**
 * @author Administrator
 */
public class DBService {
	private Logger logger = LoggerFactory.getLogger("ProcLogger");

	public void updateBalance(BalanceEntry entry) {
		Connection con = null;
		PreparedStatement pStmt = null;
		try {
			con = GlobalVar.dbConnection.getConnection();
			pStmt = con.prepareStatement("update balance_master set Wallet_flag=?,wallet=?,credits=? where user_id=?");
			pStmt.setString(1, entry.getWalletFlag());
			pStmt.setDouble(2, entry.getWalletAmount());
			pStmt.setLong(3, entry.getCredits());
			pStmt.setInt(4, entry.getUserId());
			pStmt.executeUpdate();
		} catch (Exception ex) {
			logger.error(entry.toString(), ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (con != null) {
				GlobalVar.dbConnection.putConnection(con);
			}
		}
	}

	public Map<Integer, String> listPassword() {
		Connection connection = null;
		ResultSet res = null;
		PreparedStatement pStmt = null;
		Map<Integer, String> map = new java.util.HashMap<Integer, String>();
		try {
			connection = GlobalVar.dbConnection.getConnection();
			pStmt = connection.prepareStatement("select id,driver from driver_info");
			res = pStmt.executeQuery();
			while (res.next()) {
				map.put(res.getInt("id"), new PasswordConverter().convertToEntityAttribute(res.getString("driver")));
			}
		} catch (Exception ex) {
			logger.error(" listPassword(1)", ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (connection != null) {
				GlobalVar.dbConnection.putConnection(connection);
			}
		}
		return map;
	}

	public Map<Integer, String> listPassword(Set<Integer> set) {
		Connection connection = null;
		ResultSet res = null;
		PreparedStatement pStmt = null;
		Map<Integer, String> map = new java.util.HashMap<Integer, String>();
		String sql = null;
		try {
			sql = "select id,driver from driver_info where id in("
					+ set.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
			System.out.println("SQL: " + sql);
			connection = GlobalVar.dbConnection.getConnection();
			pStmt = connection.prepareStatement(sql);
			res = pStmt.executeQuery();
			while (res.next()) {
				map.put(res.getInt("id"), new PasswordConverter().convertToEntityAttribute(res.getString("driver")));
			}
		} catch (Exception ex) {
			logger.error(" listPassword(2)", ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (connection != null) {
				GlobalVar.dbConnection.putConnection(connection);
			}
		}
		return map;
	}

	public String listPassword(int userId) {
		Connection connection = null;
		ResultSet res = null;
		PreparedStatement pStmt = null;
		String sql = null;
		String password = null;
		try {
			sql = "select id,driver from driver_info where id = ?";
			System.out.println("SQL: " + sql);
			connection = GlobalVar.dbConnection.getConnection();
			pStmt = connection.prepareStatement(sql);
			pStmt.setInt(1, userId);
			res = pStmt.executeQuery();
			if (res.next()) {
				password = new PasswordConverter().convertToEntityAttribute(res.getString("driver"));
			}
		} catch (Exception ex) {
			logger.error(" listPassword(3)", ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (connection != null) {
				GlobalVar.dbConnection.putConnection(connection);
			}
		}
		return password;
	}

	

	public Map<Integer, String> listSmscDriver() {
		Connection connection = null;
		ResultSet res = null;
		PreparedStatement pStmt = null;
		Map<Integer, String> map = new java.util.HashMap<Integer, String>();
		try {
			connection = GlobalVar.dbConnection.getConnection();
			pStmt = connection.prepareStatement("select id,driver from sdriver_info");
			res = pStmt.executeQuery();
			while (res.next()) {
				map.put(res.getInt("id"), new PasswordConverter().convertToEntityAttribute(res.getString("driver")));
			}
		} catch (Exception ex) {
			logger.error(" listSmscDriver(1)", ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (connection != null) {
				GlobalVar.dbConnection.putConnection(connection);
			}
		}
		return map;
	}
	
	public String getSmscDriver(int smscId) {
		Connection connection = null;
		ResultSet res = null;
		PreparedStatement pStmt = null;
		String sql = null;
		String password = null;
		try {
			sql = "select id,driver from sdriver_info where id = ?";
			System.out.println("SQL: " + sql);
			connection = GlobalVar.dbConnection.getConnection();
			pStmt = connection.prepareStatement(sql);
			pStmt.setInt(1, smscId);
			res = pStmt.executeQuery();
			if (res.next()) {
				password = new PasswordConverter().convertToEntityAttribute(res.getString("driver"));
			}
		} catch (Exception ex) {
			logger.error(" listSmscDriver(" + smscId + ")", ex);
		} finally {
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (connection != null) {
				GlobalVar.dbConnection.putConnection(connection);
			}
		}
		return password;
	}
}
