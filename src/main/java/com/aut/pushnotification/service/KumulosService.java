package com.aut.pushnotification.service;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class KumulosService {
	
	
	public ResponseEntity sendNotification() throws IOException {
		
		 final MediaType JSON = MediaType.get("application/json; charset=utf-8");
		    OkHttpClient client = new OkHttpClient();
		    RequestBody body = RequestBody.create(JSON, "{\r\n" + 
		    		"        \"target\": { \"broadcast\": true },\r\n" + 
		    		"        \"content\": {\"title\": \"Hello World\", \"message\": \"from Kumulos Push\" }\r\n" + 
		    		"    }");
		    Request request = new Request.Builder()
		        .url("https://messages.kumulos.com/v2/notifications")
		        .addHeader("Authorization", "Basic MjQ5OWZiYWMtMzliNC00NTI2LTlkNjQtY2Y0ZjE4YTIwMzhkOkU5Q1F2YUdvdTdCY29Ib0NrWEJzM0dnWjZIZlJoT29ZakFyRg==")
		        .addHeader("Content-Type", "application/json")
		        .addHeader("Accept", "application/json")
		        .post(body)
		        .build();

		    Response response = null;
		    response = client.newCall(request).execute();
		    String responsebody = response.body().string();
		    String respdata = response.message().toString();
		    int responsecode = response.code();
		    System.out.println("respdata : " + respdata);
		    System.out.println("responsecode : "+ responsecode);
		    
		   // ResponseEntity resp = new ResponseEntity("", responsecode);
		    return new ResponseEntity(responsebody,HttpStatus.ACCEPTED);
	}

}
