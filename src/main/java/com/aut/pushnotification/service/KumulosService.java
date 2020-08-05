package com.aut.pushnotification.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aut.pushnotification.model.PushNotificationRequest;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class KumulosService {
	
	
	
	
	public ResponseEntity sendNotification(PushNotificationRequest pushrequest) throws IOException {
		
		String title = pushrequest.getTitle();
		String message = pushrequest.getMessage();
		String[] installids = pushrequest.getInstallids();
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		
		 final MediaType JSON = MediaType.get("application/json; charset=utf-8");
		    OkHttpClient client = new OkHttpClient();
		    
			/*
			 * String jsonbody = "{\r\n" +
			 * "        \"target\": "\{installIds:"+ installids+" },\r\n" +
			 * "        \"content\": {\"title\": \"Hi Vibhu install car\", \"message\": \"How are you\" }\r\n"
			 * + "    }";
			 */
		   
		    		
		    	String test = 	"{\n" + "    \"target\": {\n" + "        \"broadcast\": false,\n" + "        \"installIds\": [  \""
		    		+ installids[0] + "\"\n" + "        ]\n" + "    },\n" +"   \"runBackgroundHandler\": true,\r\n"+ "    \"content\": {\n" + "        \"title\": \""
		    		+ title + "\",\n" + "        \"message\": \"" + message + " "
		    		+ "this push notification is sent to you on " + " " + formatter.format(date) + " "
		    		+ "\"\n" + "    }\n" + "}";

		    		
	
	
		    System.out.println("The value of the JSON body is : " + test);
			/*
			 * RequestBody body = RequestBody.create(JSON, "{\r\n" +
			 * "        \"target\": {  \"installIds\":" + installids+ "},\r\n" +
			 * "        \"content\": {\"title\":"+title+ ","
			 * +" \"message\":"+message+" }\r\n" + "    }");
			 */
		    
		    RequestBody body = RequestBody.create(JSON, test);
		    System.out.println("The value of the body is : " + body.toString());
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
