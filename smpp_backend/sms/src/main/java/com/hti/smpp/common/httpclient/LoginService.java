package com.hti.smpp.common.httpclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {
	@Autowired
	IDatabaseService dbService;

	public String getCountryname(String ip_address) {
		return dbService.getCountryname(ip_address);
	}

}
