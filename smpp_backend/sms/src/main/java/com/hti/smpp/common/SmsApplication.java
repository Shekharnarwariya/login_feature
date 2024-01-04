package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hti.smpp.common.util.GlobalVars;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class SmsApplication {

	@PostConstruct
	public void postConstruct() {
		System.out.println("Bean is constructed. Performing post-construction tasks.");
	}

	@PreDestroy
	public void preDestroy() {
		GlobalVars.hazelInstance.shutdown();
		System.out.println("Bean is about to be destroyed. Performing pre-destruction tasks.");
	}

	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class, args);
	}
}
