package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class NotificationResponseObj extends GenericResponse {
	
	private int notificationOutcomeCode;
	private String notificationOutcomeMessage;
	private String notificationInternalMessage;
	private String error;
	private Notification notification;
	

}
