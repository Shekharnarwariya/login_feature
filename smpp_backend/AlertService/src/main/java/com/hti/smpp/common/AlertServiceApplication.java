package com.hti.smpp.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

<<<<<<< HEAD
import com.hti.smpp.common.alertThreads.AlertThread;

import com.hti.smpp.common.alertThreads.AlertThread;
import org.springframework.context.ApplicationContext;
=======
import com.hti.smpp.common.alertThreads.MISCounterThread;

>>>>>>> 5703ebb (alert)
@SpringBootApplication
public class AlertServiceApplication {
	@Autowired
	private ApplicationContext applicationContext;

<<<<<<< HEAD
<<<<<<< HEAD
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
        AlertThread alert = context.getBean(AlertThread.class);
        alert.startThread();
    }
=======
	public static void main(String[] args) {
		 ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
		
		  AlertThread alert = context.getBean(AlertThread.class);
	        alert.startThread();
		
=======
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(AlertServiceApplication.class, args);
//		AlertThread alert = context.getBean(AlertThread.class);
//		alert.startThread();

		new MISCounterThread();
		
		
	

>>>>>>> 5703ebb (alert)
	}

>>>>>>> 16fcd37 (misCounter)
}
