/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.httpclient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.hti.rmi.LookupReport;

/**
 *
 * LookupService Interface to Communicate Between HLR Server & Websmpp
 */
public interface LookupService extends Remote {
	List<LookupReport> getLookupReport(Map<String, String> params) throws RemoteException;

	int reCheckStatus(String sql) throws RemoteException;
}
