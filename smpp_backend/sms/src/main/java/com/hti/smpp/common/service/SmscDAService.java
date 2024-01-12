package com.hti.smpp.common.service;

import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface SmscDAService {
	
	public Map<Integer, String> listNames();

	public Map<Integer, String> listGroupNames();

}
