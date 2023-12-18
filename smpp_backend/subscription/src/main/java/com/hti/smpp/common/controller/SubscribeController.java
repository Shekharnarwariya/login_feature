package com.hti.smpp.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.service.SubscribeService;

@RestController
public class SubscribeController {
	
	@Autowired
	private SubscribeService subscribeService;

}
