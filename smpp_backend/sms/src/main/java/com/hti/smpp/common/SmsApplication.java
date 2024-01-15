package com.hti.smpp.common;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hti.smpp.common.messages.dto.BulkContentEntry;
import com.hti.smpp.common.util.GlobalVars;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling

public class SmsApplication implements CommandLineRunner {

	@PersistenceContext
	private EntityManager entityManager;

	private final static Logger logger = LoggerFactory.getLogger(SmsApplication.class);

	@PostConstruct
	public void postConstruct() {
		logger.info("SMS Application is about to start.");
	}

	@PreDestroy
	public void preDestroy() {
		GlobalVars.hazelInstance.shutdown();
		logger.info("SMS Application is about to stop.");
	}

	public static void main(String[] args) {
		SpringApplication.run(SmsApplication.class, args);
		logger.info("SMS Application started successfully.");

	}

	@Override
	public void run(String... args) throws Exception {
		String jpqlQuery = "SELECT new com.hti.smpp.common.messages.dto.BulkContentEntry(bc.id, bc.destination, bc.content, bc.flag) "
				+ "FROM BatchContentEntity bc WHERE bc.batchId = :id";

		Query jpqlNativeQuery = entityManager.createQuery(jpqlQuery, BulkContentEntry.class);
		jpqlNativeQuery.setParameter("id", 60);

		List<BulkContentEntry> bulkContentEntries = (List<BulkContentEntry>) jpqlNativeQuery.getResultList();

	}

}
