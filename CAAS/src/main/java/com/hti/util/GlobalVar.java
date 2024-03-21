package com.hti.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import com.hti.database.DBConnection;
import com.hti.route.dto.HlrRouteEntry;
import com.hti.route.dto.MmsRouteEntry;
import com.hti.route.dto.OptionalRouteEntry;
import com.hti.route.dto.RouteEntry;
import com.hti.smpp.common.flag.FlagDTO;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.util.BatchObject;
import com.hti.smsc.dto.GroupEntry;
import com.hti.smsc.dto.GroupMemberEntry;
import com.hti.smsc.dto.SmscEntry;
import com.hti.user.dto.BalanceEntry;
import com.hti.user.dto.DlrSettingEntry;
import com.hti.user.dto.ProfessionEntry;
import com.hti.user.dto.UserEntry;
import com.hti.user.dto.WebMasterEntry;

public class GlobalVar {
	public static ApplicationContext context = new FileSystemXmlApplicationContext("config/CaasContext.xml");
	public static boolean APPLICATION_STOP;
	public static boolean MASTER_MEMBER = false;
	public static HazelcastInstance hazelInstance;
	public static IMap<Integer, NetworkEntry> network_entries;
	public static IMap<Integer, FlagDTO> flag_write_Cache;
	public static IMap<Integer, SmscEntry> smsc_entries;
	public static IMap<String, Integer> smsc_name_mapping;
	public static IMap<Integer, GroupEntry> smsc_group;
	public static MultiMap<Integer, GroupMemberEntry> smsc_group_member;
	public static IMap<Integer, UserEntry> user_entries;
	public static IMap<Integer, BalanceEntry> balance_entries;
	public static IMap<String, Integer> user_mapping;
	public static IMap<Integer, ProfessionEntry> profession_entries;
	public static IMap<Integer, WebMasterEntry> webmaster_entries;
	public static IMap<Integer, DlrSettingEntry> dlrSetting_entries;
	// public static Map<Integer, ProfileAllocEntry> routing_profile_alloc;
	public static IMap<Integer, RouteEntry> basic_routing;
	public static IMap<Integer, HlrRouteEntry> hlr_routing;
	public static IMap<Integer, MmsRouteEntry> mms_routing;
	public static IMap<Integer, OptionalRouteEntry> optional_routing;
	// public static MultiMap<Integer, Integer> user_route_mapping;
	public static IMap<String, String> user_flag_status;
	public static IMap<String, Properties> smsc_flag_status;
	public static IMap<Integer, Integer> smsc_submit_counter;
	public static IMap<String, Map<Long, Integer>> user_submit_counter;
	public static IMap<Integer, BatchObject> BatchQueue;
	public static IMap<Integer, BatchObject> HlrBatchQueue;
	public static Map<Integer, Map<Integer, Map<String, String>>> SmscScheduleConfig = new HashMap<Integer, Map<Integer, Map<String, String>>>();
	public static IMap<String, String> HttpDlrParam;
	// daoimpl
	public static com.hti.dao.UserDAService userService = new com.hti.dao.impl.UserDAServiceImpl();
	public static com.hti.dao.NetworkDAService networkService = new com.hti.dao.impl.NetworkDAServiceImpl();
	public static com.hti.dao.RouteDAService routeService = new com.hti.dao.impl.RouteDAServiceImpl();
	public static com.hti.dao.SmscDAService smscService = new com.hti.dao.impl.SmscDAServiceImpl();
	public static com.hti.database.DBService dbService = new com.hti.database.DBService();
	// -------------- Database Details -------------
	public static String DATABASE_URL = "jdbc:mysql://localhost:3306/host";
	public static String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
	public static String DATABASE_USER = "smppuser";
	public static String DATABASE_PASSWORD = "smpp";
	public static int MAX_CONNECTION = 20;
	public static DBConnection dbConnection;
}
