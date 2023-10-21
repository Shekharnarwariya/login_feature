package com.hti.smpp.common.sms.service;

import java.util.Map;

public interface SmscDAService {
	public Map<Integer, String> listNames();

	public Map<Integer, String> listGroupNames();

}
