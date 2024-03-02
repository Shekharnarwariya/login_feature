package com.hti.smpp.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.hti.smpp.common.alertThreads.AlertThread;

@SpringBootApplication
public class AlertServiceApplication {
    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
        AlertThread alert = context.getBean(AlertThread.class);
        alert.startThread();
    }
}
