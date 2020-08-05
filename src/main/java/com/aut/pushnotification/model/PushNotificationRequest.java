package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class PushNotificationRequest {
	
	private String title;
	private String message;
	private String[] installids;

}
