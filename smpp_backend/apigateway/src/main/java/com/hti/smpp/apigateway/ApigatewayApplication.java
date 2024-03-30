package com.hti.smpp.apigateway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableDiscoveryClient
@SpringBootApplication
public class ApigatewayApplication {

	private final static Logger logger = LoggerFactory.getLogger(ApigatewayApplication.class);

	@PostConstruct
	public void postConstruct() throws IOException {
		logger.info(" Apigateway Application is about to start.");
		//System.out.println(Files.readString(Path.of(new ClassPathResource("welcome.fil").getURI())));
	}

	@PreDestroy
	public void preDestroy() {

		logger.info(" Apigateway Application is about to stop.");
	}

	public static void main(String[] args) {
		SpringApplication.run(ApigatewayApplication.class, args);
		logger.info("Apigateway Application started successfully.");
	}

}
