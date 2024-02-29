package com.hti.smpp.common.alertThreads;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.dto.Network;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.util.MessageResourceBundle;

@Component
@Service
public class AlertThreadDbInfo {

	private Logger logger = LoggerFactory.getLogger(AlertThreadDbInfo.class);

	private DataSource dataSource;
	
	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	public AlertThreadDbInfo(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public Map<String, String> checkCustomSettings() {
		Map<String, String> custom = new HashMap<String, String>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet res = null;
		System.out.println("DataSource: " + this.dataSource);
		logger.info(messageResourceBundle.getLogMessage("dataSource.info"), this.dataSource);
		try {
			con = getConnection();
			String query1 = "select username,price_change_subject from custom_setting";
			pStmt = con.prepareStatement(query1);
			res = pStmt.executeQuery();
			while (res.next()) {
				String username = res.getString("username");
				String price_change_subject = res.getString("price_change_subject");
				if (price_change_subject != null && price_change_subject.length() > 0) {
					custom.put(username, price_change_subject);
				}
			}
			// logger.info("CUSTOM_SUBJECT_DB: ==>" + custom);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
//					dbCon.releaseConnection(con);
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return custom;
	}

	public Map<Integer, List<RouteEntryExt>> checkForPriceChange() throws SQLException {
		Map<Integer, List<RouteEntryExt>> routing = new HashMap<Integer, List<RouteEntryExt>>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet res = null;
		try {
			con = getConnection();
			String query1 = "select id,user_id,network_id,CAST(cost_old AS CHAR) AS cost_old,CAST(cost_new AS CHAR) AS cost_new,smsc_old_id,smsc_new_id,affected_date from routemaster_updates where flag='false'";
			pStmt = con.prepareStatement(query1);
			res = pStmt.executeQuery();
			RouteEntryExt routingDTO = null;
			RouteEntry entry = null;
			while (res.next()) {
				double cost_old = res.getDouble("cost_old");
				double cost_new = res.getDouble("cost_new");
				String smsc_old = res.getString("smsc_old_id");
				String smsc_new = res.getString("smsc_new_id");
				String remarks = "-";
				if (smsc_old != null && smsc_old.length() > 0) {
					if (cost_old != cost_new) {
						if (cost_new > cost_old) {
							remarks = "Increased";
						} else {
							remarks = "Decreased";
						}
					} else if (!smsc_old.equalsIgnoreCase(smsc_new)) {
						remarks = "Updated";
					}
				} else {
					remarks = "Added";
				}
				entry = new RouteEntry();
				routingDTO = new RouteEntryExt(entry);
				int networkId = res.getInt("network_id");
				entry.setId(res.getInt("id"));
				entry.setUserId(res.getInt("user_id"));
				entry.setNetworkId(networkId);
				entry.setEditOn(res.getString("affected_date"));
				routingDTO.setOldCost(res.getString("cost_old"));
				routingDTO.setCostStr(res.getString("cost_new"));
				routingDTO.setRemarks(remarks);
				List<RouteEntryExt> list = null;
				if (routing.containsKey(networkId)) {
					list = routing.get(networkId);
				} else {
					list = new ArrayList<RouteEntryExt>();
				}
				list.add(routingDTO);
				routing.put(networkId, list);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle.getMessage());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
//					dbCon.releaseConnection(con);
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return routing;
	}

	public Map<Integer, List<RouteEntryExt>> checkForPriceChangeSch() throws SQLException {
		Map<Integer, List<RouteEntryExt>> routing = new HashMap<Integer, List<RouteEntryExt>>();
		Connection con = null;
		PreparedStatement pStmt1 = null;
		ResultSet res = null;
		try {
			con = getConnection();
			String query1 = "select A.id,A.user_id,A.network_id,A.schedule_on,CAST(B.cost AS CHAR) AS cost_old,CAST(A.cost AS CHAR) AS cost_new from routemaster_sch A,routemaster B where A.flag='false' and A.id=B.id";
			pStmt1 = con.prepareStatement(query1);
			res = pStmt1.executeQuery();
			RouteEntryExt routingDTO = null;
			RouteEntry entry = null;
			while (res.next()) {
				double cost_old = res.getDouble("cost_old");
				double cost_new = res.getDouble("cost_new");
				String remarks = "-";
				if (cost_old != cost_new) {
					if (cost_new > cost_old) {
						remarks = "Increased";
					} else {
						remarks = "Decreased";
					}
				} else {
					continue;
				}
				entry = new RouteEntry();
				routingDTO = new RouteEntryExt(entry);
				int networkId = res.getInt("A.network_id");
				entry.setId(res.getInt("A.id"));
				entry.setUserId(res.getInt("A.user_id"));
				entry.setNetworkId(networkId);
				entry.setEditOn(res.getString("A.schedule_on"));
				routingDTO.setOldCost(res.getString("cost_old"));
				routingDTO.setCostStr(res.getString("cost_new"));
				routingDTO.setRemarks(remarks);
				List<RouteEntryExt> list = null;
				if (routing.containsKey(networkId)) {
					list = routing.get(networkId);
				} else {
					list = new ArrayList<RouteEntryExt>();
				}
				list.add(routingDTO);
				routing.put(networkId, list);
			}
		} catch (SQLException sqle) {
			throw new SQLException(sqle.getMessage());
		} finally {
			try {
				if (res != null) {
					res.close();
					res = null;
				}
				if (pStmt1 != null) {
					pStmt1.close();
					pStmt1 = null;
				}
				if (con != null) {
//					dbCon.releaseConnection(con);
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return routing;
	}

	public Map<Integer, Network> getNetworkRecord(Set<Integer> idSet) throws SQLException {
		Map<Integer, Network> networklist = new HashMap<Integer, Network>();
		Connection con = null;
		PreparedStatement pStmt = null;
		ResultSet rs = null;
		String ids = "";
		String sql = "";
		for (int next : idSet) {
			ids += next + ",";
		}
		if (ids.length() > 1) {
			ids = ids.substring(0, ids.length() - 1);
		}
		// logger.info("ID ====> " + ids);
		sql = "select * from network where id in(" + ids + ")";
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			// pStmt.setString(1, ids);
			rs = pStmt.executeQuery();
			Network network = null;
			while (rs.next()) {
				network = new Network();
				network.setId(rs.getInt("id"));
				network.setCc(rs.getString("cc"));
				network.setMcc(rs.getString("mcc"));
				network.setMnc(rs.getString("mnc"));
				network.setCountry(rs.getString("country"));
				network.setOperator(rs.getString("operator"));
				networklist.put(rs.getInt("id"), network);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException sqle) {
				}
			}
			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException sqle) {
				}
			}
			if (con != null) {
//				dbCon.releaseConnection(con);
				con.close();
			}
		}
		return networklist;
	}

	public boolean updateRoutingFlagSch(Map list) {
		boolean isUpdate = false;
		Connection con = null;
		String sql = "update routemaster_sch set flag=? where id=?";
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			con.setAutoCommit(false);
			Iterator itr = list.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				pStmt.setString(1, (String) entry.getValue());
				pStmt.setInt(2, (Integer) entry.getKey());
				pStmt.addBatch();
			}
			int[] executeBatch = pStmt.executeBatch();
			con.commit();
			if (executeBatch.length > 0) {
				isUpdate = true;
			}
			logger.info(messageResourceBundle.getLogMessage("routingFlag.info"), executeBatch.length);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
//					dbCon.releaseConnection(con);
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return isUpdate;
	}

	public boolean updateRoutingFlag(Map list) {
		boolean isUpdate = false;
		Connection con = null;
		String sql = "update routemaster_updates set flag=? where id=?";
		PreparedStatement pStmt = null;
		try {
			con = getConnection();
			pStmt = con.prepareStatement(sql);
			con.setAutoCommit(false);
			Iterator itr = list.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry entry = (Map.Entry) itr.next();
				pStmt.setString(1, (String) entry.getValue());
				pStmt.setInt(2, (Integer) entry.getKey());
				pStmt.addBatch();
			}
			int[] executeBatch = pStmt.executeBatch();
			con.commit();
			if (executeBatch.length > 0) {
				isUpdate = true;
			}
			logger.info(messageResourceBundle.getLogMessage("routingFlag.info"), executeBatch.length);
		} catch (SQLException sqle) {
			logger.error(" ", sqle.fillInStackTrace());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
					pStmt = null;
				}
				if (con != null) {
//					dbCon.releaseConnection(con);
					con.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return isUpdate;
	}

}
