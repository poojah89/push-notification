package com.aut.pushnotification.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NotifiRequest {

	
	@Schema(example = "Hello from AUT scheduled message")
	private String title;

	
	@Schema(example = "Hi user")
	private String message;

	
	@Schema(example = "Useridstest2CSV1")
	private String channelName;

	
	@Schema(example = "true")
	private boolean scheduled;

	
	@Schema(example = "2020-06-03 09:20")
	private String sendAt;

	
	@Schema(example = "local")
	private String strategy;

	
	@Schema(example = "ignore")
	private String pastTimes;
}
