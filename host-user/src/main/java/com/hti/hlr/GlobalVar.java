/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.hlr;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hti.objects.HTIQueue;
import com.logica.smpp.util.Queue;

/**
 *
 * @author Administrator
 */
public class GlobalVar {
	public static HTIQueue lookupStatusInsertQueue = new HTIQueue();
	public static Map<String, HlrRequestHandler> HlrRequestHandlers = new ConcurrentHashMap<String, HlrRequestHandler>();
	public static Map<String, RouteObject> EnqueueRouteObject = new ConcurrentHashMap<String, RouteObject>();
	public static Map<String, String> HlrResponseMapping = new ConcurrentHashMap<String, String>();
	public static Map<String, Queue> HlrResponeQueue = new ConcurrentHashMap<String, Queue>();
	public static Queue MismatchedWaiting = new Queue();
}
