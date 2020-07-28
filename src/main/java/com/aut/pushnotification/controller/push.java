package com.aut.pushnotification.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aut.pushnotification.service.KumulosService;

@RestController
@RequestMapping("/api")
public class push {
	
	
	@Autowired
	KumulosService kumulosService;
	
@GetMapping("/push")	
public ResponseEntity getPushNotification() throws IOException {
	
	ResponseEntity resp = kumulosService.sendNotification();
	
	
	return resp;
	
	
}


}
