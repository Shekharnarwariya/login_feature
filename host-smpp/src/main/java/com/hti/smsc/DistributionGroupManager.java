package com.hti.smsc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.multimap.MultiMap;
import com.hti.smsc.dto.GroupMemberEntry;
import com.hti.util.GlobalCache;
import com.hti.util.GlobalVars;

/*
 * To change the route(SMSC) of the pkt for decreasing the traffic in single SMSC while others can be used.
 */
public class DistributionGroupManager {
	private static Map<Integer, List<String>> GROUP_SMSC = new ConcurrentHashMap<Integer, List<String>>();
	private static Logger logger = LoggerFactory.getLogger(DistributionGroupManager.class);
	private static Map<Integer, Map<String, Integer>> GROUP_SMSC_LIMIT = new HashMap<Integer, Map<String, Integer>>(); // <group_id,Map<route,percentage>>
	private static Map<Integer, Map<String, Integer>> GROUP_SMSC_SUBMIT = new HashMap<Integer, Map<String, Integer>>(); // <group_id,Map<route,submitCount>>
	private static Map<Integer, List<String>> GROUP_SMSC_SEQUENCE = new HashMap<Integer, List<String>>(); // <group_id,List<route>>
	private static boolean PROCEED; // to instruct if able to find the route for incoming request
	private static Map<Integer, String> GROUP_MEMBER = new ConcurrentHashMap<Integer, String>(); // group to map Single route <group_id,route>
	private static MultiMap<Integer, GroupMemberEntry> SmscGrouping = GlobalVars.hazelInstance
			.getMultiMap("smsc_group_member");
	// private static DB repeatedNumberDatabase = DBMaker.fileDB("backup//groupRepeatedNumber.db").make();

	public static synchronized boolean initialize() {
		logger.info("****** Checking For Distribution ***** ");
		GROUP_SMSC.clear();
		GROUP_SMSC_LIMIT.clear();
		GROUP_SMSC_SUBMIT.clear();
		GROUP_SMSC_SEQUENCE.clear();
		if (SmscGrouping.size() > 0) {
			try {
				Map<Integer, Integer> percent_calculation = new HashMap<Integer, Integer>();
				try {
					for (GroupMemberEntry entry : SmscGrouping.values()) {
						if (entry.getGroupId() > 0 && GlobalCache.SmscGroupEntries.containsKey(entry.getGroupId())) {
							if (percent_calculation.containsKey(entry.getGroupId())) {
								int percent_sum = percent_calculation.get(entry.getGroupId()) + entry.getPercent();
								percent_calculation.put(entry.getGroupId(), percent_sum);
							} else {
								percent_calculation.put(entry.getGroupId(), entry.getPercent());
							}
						}
					}
					List<String> smsc_list = null;
					Map<String, Integer> smsc_limit = null;
					List<String> smsc_sequence = null;
					for (Map.Entry<Integer, Integer> group_percent : percent_calculation.entrySet()) {
						int group_id = group_percent.getKey();
						if (group_percent.getValue() == 100) {
							for (GroupMemberEntry entry : SmscGrouping.get(group_id)) {
								if (GROUP_SMSC.containsKey(group_id)) {
									smsc_list = GROUP_SMSC.remove(group_id);
								} else {
									smsc_list = new ArrayList<String>();
								}
								smsc_list.add(entry.getSmsc());
								GROUP_SMSC.put(group_id, smsc_list);
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
				if (GROUP_SMSC.isEmpty()) {
					PROCEED = false;
					logger.info("**** No Valid Distribution Configured ****");
				} else {
					logger.info("Distribution: " + GROUP_SMSC);
					PROCEED = true;
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		} else {
			PROCEED = false;
		}
		return PROCEED;
	}

	public static synchronized String findRoute(int group_id) {
		String next_smsc = null;
		if (PROCEED) {
			try {
				if (GROUP_SMSC.containsKey(group_id)) {
					if (!(GROUP_SMSC.get(group_id)).isEmpty()) {
						next_smsc = (GROUP_SMSC.get(group_id)).remove(0);
						(GROUP_SMSC.get(group_id)).add(next_smsc); // add to last
					}
				}
			} catch (Exception ex) {
				logger.error(group_id + "", ex);
			}
		} else {
			logger.debug("Distribution Service Not Available");
		}
		return next_smsc;
	}

	public static synchronized Set<String> listMembers(int group_id) {
		Set<String> members = new HashSet<String>();
		if (GROUP_SMSC.containsKey(group_id)) {
			members.addAll(GROUP_SMSC.get(group_id));
		}
		return members;
	}

	public static synchronized int memberCount(int group_id) {
		if (GROUP_SMSC.containsKey(group_id)) {
			return (GROUP_SMSC.get(group_id)).size();
		}
		return 0;
	}

	public static synchronized String findPercentRoute(int group_id) {
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
