package com.hti.util;

import com.hazelcast.core.HazelcastInstance;
import com.hti.dao.NetworkDAService;
import com.hti.dao.SmscDAService;
import com.hti.dao.UserDAService;

public class GlobalVars {
	public static HazelcastInstance hazelInstance;
	public static boolean MASTER_CLIENT = false;
	public static boolean DB_CLUSTER = false;
	public static int SERVER_ID = 0;
	public static UserDAService userService;
	public static SmscDAService smscService;
	public static NetworkDAService networkService;
}
