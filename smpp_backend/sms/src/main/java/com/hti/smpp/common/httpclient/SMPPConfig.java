package com.hti.smpp.common.httpclient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.logica.smpp.util.Queue;

@Configuration
public class SMPPConfig {

	@Bean
	public Queue smppQueue() {
		return new Queue();
	}
}
