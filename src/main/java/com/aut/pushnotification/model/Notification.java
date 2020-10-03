package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class Notification {
	
	private String id;
	private String status;
	private String title;
	private String message;
	private String isBackgroundData;
	private String url;
	private String createdAt;
	private String updatedAt;
	private String messageId;
	private String appId;
	private String source;
	private Filters filters;
	private Schedule schedule;
	
	

}
