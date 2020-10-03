package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class CancelScheduledNotificationResponse extends GenericResponse {
	
	private String error;

}
