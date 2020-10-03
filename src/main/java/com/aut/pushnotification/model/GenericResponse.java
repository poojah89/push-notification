package com.aut.pushnotification.model;

import lombok.Data;

@Data
public class GenericResponse {
	private int outcomeCode;
	private String outcomeMessage;
	private String internalMessage;
}
