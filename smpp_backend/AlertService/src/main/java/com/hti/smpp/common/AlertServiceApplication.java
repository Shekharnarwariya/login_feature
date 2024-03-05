package com.hti.smpp.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.hti.smpp.common.alertThreads.AlertThread;
import com.hti.smpp.common.alertThreads.MISCounterThread;

@SpringBootApplication
public class AlertServiceApplication {
    
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
		AlertThread alert = context.getBean(AlertThread.class);
		alert.startThread();
		MISCounterThread mis = context.getBean(MISCounterThread.class);
		mis.startMisThread();
	}
}
