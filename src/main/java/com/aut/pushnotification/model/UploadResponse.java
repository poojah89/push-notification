package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class UploadResponse extends NotificationResponseObj {

	private String fileName;
	private int uploadOutcomeCode;
	private String uploadOutcomeMessage;
	private String uploadInternalMessage;
	private int channelCreationOutcomeCode;
	private String channelCreationOutcomeMessage;
	private String channelCreationInternalMessage;
	
}
