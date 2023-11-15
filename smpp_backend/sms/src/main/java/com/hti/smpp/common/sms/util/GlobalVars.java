/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.sms.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.hti.smpp.common.dto.BatchObject;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.route.dto.HlrRouteEntry;
import com.hti.smpp.common.route.dto.OptionalRouteEntry;
import com.hti.smpp.common.sms.service.SmscDAService;
import com.hti.smpp.common.sms.session.SessionHandler;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;

@Component
public class GlobalVars {
	public static Map<String, SessionHandler> UserSessionHandler = Collections
			.synchronizedMap(new HashMap<String, SessionHandler>());

	public static Map<String, Set<Integer>> ScheduledBatches = new ConcurrentHashMap<String, Set<Integer>>();

	public static Set<Integer> RepeatedSchedules = Collections.synchronizedSet(new HashSet<Integer>());

	public static Map<Integer, UserEntry> UserEntries;

	public static SmscDAService smscService;

	public static Map<Integer, WebMasterEntry> WebmasterEntries;

	public static Map<Integer, NetworkEntry> NetworkEntries;

	public static Map<Integer, HlrRouteEntry> HlrRouteEntries;

	public static Map<Integer, OptionalRouteEntry> OptionalRouteEntries;

	public static Map<String, Integer> PrefixMapping = Collections.synchronizedMap(new HashMap<String, Integer>());

	public static Map<Integer, BatchObject> BatchQueue;
	
}