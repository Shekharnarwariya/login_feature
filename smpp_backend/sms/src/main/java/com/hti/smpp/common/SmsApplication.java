package com.hti.smpp.common;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.util.GlobalVars;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableScheduling
public class SmsApplication implements CommandLineRunner{

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
		String url="https://exquisite-quokka-5d0794.netlify.app/";
			System.out.println("short url call   time" + LocalTime.now());
			String accessToken = "9b23160c57745c45f1e9e96a66e6cd5fc0f3bb07";
			String apiUrl = "https://api-ssl.bitly.com/v4/shorten";
			String shortenedUrl = null;
			try {
				URL bitlyUrl = new URL(apiUrl);
				HttpURLConnection connection = (HttpURLConnection) bitlyUrl.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Accept", "application/json");
				connection.setRequestProperty("Authorization", "Bearer " + accessToken);

				String jsonInputString = "{\"long_url\":\"" + url + "\"}";
				try (OutputStream os = connection.getOutputStream()) {
					byte[] input = jsonInputString.getBytes("utf-8");
					os.write(input, 0, input.length);
				}

				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_CREATED) {
					try (Scanner scanner = new Scanner(connection.getInputStream())) {
						StringBuilder response = new StringBuilder();
						while (scanner.hasNextLine()) {
							response.append(scanner.nextLine());
						}

						// Extract the shortened URL from the response
						shortenedUrl = response.toString().split("\"link\":\"")[1].split("\"")[0];
						System.out.println("Short URL: " + shortenedUrl);
					}
				} else {
					// Handle error response
					System.out.println("Error: " + responseCode);
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw new InternalServerException(e.getMessage());
			}
			System.out.println("sort url out put    time" + LocalTime.now());
			System.out.println(shortenedUrl);
		
		
	}
}
