package com.hti.smpp.personalservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableEurekaClient
@SpringBootApplication
@RestController
public class PersonalserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalserviceApplication.class, args);
	}

	@PostMapping("/personal/graphql")
	public String value(){
		return "in api personal";
	}

}
