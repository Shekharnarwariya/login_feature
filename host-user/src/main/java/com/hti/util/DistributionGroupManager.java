package com.hti.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smsc.dto.GroupMemberEntry;

/*
 * To change the route(SMSC) of the pkt for decreasing the traffic in single SMSC while others can be used.
 */
public class DistributionGroupManager {
	private static Logger logger = LoggerFactory.getLogger("ProcLogger");
	private static Map<Integer, Map<String, Integer>> GROUP_SMSC_LIMIT = new HashMap<Integer, Map<String, Integer>>(); // <group_id,Map<route,percentage>>
	private static Map<Integer, Map<String, Integer>> GROUP_SMSC_SUBMIT = new HashMap<Integer, Map<String, Integer>>(); // <group_id,Map<route,submitCount>>
	private static Map<Integer, List<String>> GROUP_SMSC_SEQUENCE = new HashMap<Integer, List<String>>(); // <group_id,List<route>>
	private static boolean PROCEED; // to instruct if able to find the route for incoming request
	private static Map<Integer, String> GROUP_MEMBER = new ConcurrentHashMap<Integer, String>(); // group to map Single route <group_id,route>

	public static synchronized boolean initialize() {
		logger.info("****** Checking For Distribution ***** ");
		PROCEED = false;
		GROUP_SMSC_LIMIT.clear();
		GROUP_SMSC_SUBMIT.clear();
		GROUP_SMSC_SEQUENCE.clear();
		if (GlobalCache.SmscGroupMember.size() > 0) {
			logger.info("SmscGrouping: " + GlobalCache.SmscGroupMember.size());
			Map<Integer, Integer> percent_calculation = new HashMap<Integer, Integer>();
			try {
				for (GroupMemberEntry entry : GlobalCache.SmscGroupMember.values()) {
					if (entry.getGroupId() > 0) {
						if (percent_calculation.containsKey(entry.getGroupId())) {
							int percent_sum = percent_calculation.get(entry.getGroupId()) + entry.getPercent();
							percent_calculation.put(entry.getGroupId(), percent_sum);
						} else {
							percent_calculation.put(entry.getGroupId(), entry.getPercent());
						}
					}
				}
				Map<String, Integer> smsc_limit = null;
				List<String> smsc_sequence = null;
				for (Map.Entry<Integer, Integer> group_percent : percent_calculation.entrySet()) {
					int group_id = group_percent.getKey();
					if (group_percent.getValue() == 100) {
						for (GroupMemberEntry entry : GlobalCache.SmscGroupMember.get(group_id)) {
							if (!GROUP_MEMBER.containsKey(group_id)) {
								GROUP_MEMBER.put(group_id, entry.getSmsc());
							}
							if (GROUP_SMSC_SEQUENCE.containsKey(group_id)) {
								smsc_sequence = GROUP_SMSC_SEQUENCE.get(group_id);
							} else {
								smsc_sequence = new ArrayList<String>();
							}
							smsc_sequence.add(entry.getSmsc());
							GROUP_SMSC_SEQUENCE.put(group_id, smsc_sequence);
							if (GROUP_SMSC_LIMIT.containsKey(group_id)) {
								smsc_limit = GROUP_SMSC_LIMIT.remove(group_id);
							} else {
								smsc_limit = new HashMap<String, Integer>();
							}
							smsc_limit.put(entry.getSmsc(), entry.getPercent());
							GROUP_SMSC_LIMIT.put(group_id, smsc_limit);
						}
					} else {
						logger.warn("Invalid Group Configured:--> " + group_id);
					}
				}
			} catch (Exception ex) {
				logger.error("initialize()", ex.fillInStackTrace());
			}
			if (GROUP_SMSC_LIMIT.isEmpty()) {
				logger.info("**** No Valid Distribution Configured ****");
			} else {
				logger.info("***********************************");
				logger.info("Distribution: " + GROUP_SMSC_LIMIT);
				logger.info("***********************************");
				PROCEED = true;
			}
		} else {
			logger.warn("***** No Smsc Grouping Configured ***** ");
		}
		return PROCEED;
	}

	public static synchronized String findMember(int group_id) {
		// String smsc_member = null;
		if (PROCEED) {
			try {
				if (GROUP_MEMBER.containsKey(group_id)) {
					return GROUP_MEMBER.get(group_id);
				}
			} catch (Exception ex) {
				logger.error(group_id + "", ex);
			}
		} else {
			logger.debug("Distribution Service Not Available");
		}
		return null;
	}

	public static synchronized String findRoute(int group_id) {
		String next_smsc = null;
		if (PROCEED) {
			try {
				Map<String, Integer> counter_map = null;
				if (GROUP_SMSC_SUBMIT.containsKey(group_id)) {
					counter_map = GROUP_SMSC_SUBMIT.get(group_id);
				} else {
					counter_map = new HashMap<String, Integer>();
				}
				int total_members = GROUP_SMSC_SEQUENCE.get(group_id).size();
				do {
					for (int i = 1; i <= total_members; i++) {
						String loop_smsc = GROUP_SMSC_SEQUENCE.get(group_id).remove(0);
						GROUP_SMSC_SEQUENCE.get(group_id).add(loop_smsc); // put at last
						int limit = GROUP_SMSC_LIMIT.get(group_id).get(loop_smsc);
						int submitted = 0;
						if (counter_map.containsKey(loop_smsc)) {
							submitted = counter_map.get(loop_smsc);
						}
						// logger.info(loop_smsc + " Limit: " + limit + " Submit: " + submitted);
						if (submitted < limit) {
							next_smsc = loop_smsc;
							counter_map.put(next_smsc, ++submitted);
							// logger.info("Final Route Found: " + next_smsc);
							break;
						}
					}
					if (next_smsc == null) {
						// logger.info("loop finished.Looping Again");
						counter_map.clear();
					}
				} while (next_smsc == null);
				GROUP_SMSC_SUBMIT.put(group_id, counter_map);
			} catch (Exception ex) {
				logger.error(group_id + "", ex);
			}
		} else {
			logger.debug("Distribution Service Not Available");
		}
		return next_smsc;
	}
}
