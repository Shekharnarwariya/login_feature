package com.hti.dao;

import java.util.Set;

import com.hti.smsc.dto.SmscEntry;

public interface SmscDAService {
	public void initializeGlobalCache();

	public void initializeStatus();

	public void initializeLimit();

	public void initSpecialSetting();

	public void initEsmeErrorConfig();

	public void initSignalErrorConfig();

	public void reloadStatus();
	
	public void initSmscBsfm();

	public Set<String> listNames();

	public SmscEntry getEntry(int smsc_id);

	public SmscEntry getEntry(String smsc_name);

	public void stopConnections();

	public void initializeLoopingRules();
}
