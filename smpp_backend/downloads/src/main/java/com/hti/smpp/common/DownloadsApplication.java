package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DownloadsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DownloadsApplication.class, args);
	}

}
