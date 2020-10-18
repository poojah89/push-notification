package com.aut.pushnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PushNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PushNotificationApplication.class, args);
	}

}
