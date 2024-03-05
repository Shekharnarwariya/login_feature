package com.hti.smpp.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
<<<<<<< HEAD
import org.springframework.scheduling.annotation.EnableScheduling;
=======

import com.hti.smpp.common.alertThreads.AlertThread;
>>>>>>> 96401f1d1d1a31c5e1b73c83ac974f4359502342

import com.hti.smpp.common.alertThreads.AlertThread;
import com.hti.smpp.common.alertThreads.MISCounterThread;

@SpringBootApplication
public class AlertServiceApplication {
    @Autowired
    private ApplicationContext applicationContext;

<<<<<<< HEAD
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
		AlertThread alert = context.getBean(AlertThread.class);
		alert.startThread();
		MISCounterThread mis = context.getBean(MISCounterThread.class);
		mis.startMisThread();
	}

=======
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
        AlertThread alert = context.getBean(AlertThread.class);
        alert.startThread();
    }
>>>>>>> 96401f1d1d1a31c5e1b73c83ac974f4359502342
}
