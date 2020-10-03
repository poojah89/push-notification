package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class ChannelResponseObj extends GenericResponse{

	private String uuid;
	private String name;
	private String showInPortal;
	private String updatedAt;
	private String createdAt;
	private String id;
	private Meta meta;
	
}
