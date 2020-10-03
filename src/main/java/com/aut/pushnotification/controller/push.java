package com.aut.pushnotification.controller;

import java.io.IOException;
import java.util.List;

import javax.websocket.Decoder.Binary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aut.pushnotification.exception.InternalException;
import com.aut.pushnotification.service.KumulosService;
import com.aut.pushnotification.model.ApplicationType;
import com.aut.pushnotification.model.CancelScheduledNotificationResponse;
import com.aut.pushnotification.model.ChannelResponseObj;
import com.aut.pushnotification.model.NotifiRequest;
import com.aut.pushnotification.model.NotificationResponseObj;
import com.aut.pushnotification.model.UploadResponse;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api")
public class push {

	@Autowired
	KumulosService fileUploadService;

	@Operation(description = "File Upload from DB", tags = "Push", summary = "Note that this operation will do the following operations, 1. create a channel on the Kumulous identical with the file name, (if there is no channel aready exists with that name). 2. Upload the CSV to Kumulous which will add users to the channel 3. Send Push Notification to the users at the scheduled time")
	@PostMapping("/CSVupload")
	public ResponseEntity UploadCSv(@RequestParam("KumulousApplication") ApplicationType applicationType)
			throws IOException, InternalException {

		List<UploadResponse> response = fileUploadService.fileUpload(applicationType);
		return new ResponseEntity(response, HttpStatus.OK);
	}

	@Operation(description = "API to send notification", tags = "Push", summary = "API to send push notification to a channel")
	@PostMapping("/sendNotification")
	public ResponseEntity SendNotification(@RequestParam("KumulousApplication") ApplicationType applicationType,
			@RequestBody NotifiRequest req) throws IOException {

		NotificationResponseObj resp = fileUploadService.processChannelNotification(applicationType, req);

		if (resp.getNotificationOutcomeCode() == 202) {

			return new ResponseEntity(resp, HttpStatus.ACCEPTED);
		}

		else {
			return new ResponseEntity(resp, HttpStatus.BAD_REQUEST);

		}
	}

	@Operation(description = "API to create a channel", tags = "Push", summary = "API to create a channel in Kumulos")
	@PostMapping("/createChannel")
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityRequirements(value = {})
	public ResponseEntity CreateChannel(@RequestParam("KumulousApplication") ApplicationType applicationType,
			@Parameter(schema = @Schema(type = "string"), name = "channelUUID",  description = "Name of the channel to be created", example = "Useridstest2CSV1", required = true) @RequestParam String channelUUID)
			throws IOException, InternalException {

		ChannelResponseObj resp = fileUploadService.createChannel(applicationType, channelUUID);

		if (resp.getOutcomeCode() == 201) {

			return new ResponseEntity(resp, HttpStatus.CREATED);

		}

		else {

			return new ResponseEntity(resp, HttpStatus.UNPROCESSABLE_ENTITY);
		}
	}

	@Operation(description = "Manual File Upload", tags = "Push", summary = "This is to upload the file via API manually, Note that this operation will do the following operations, 1. create a channel on the Kumulous identical with the file name, (if there is no channel aready exists with that name). 2. Upload the CSV to Kumulous which will add users to the channel 3. Send Push Notification to the users at the scheduled time")
	@PostMapping(value = "/fileupload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	@SecurityRequirements(value = {})
	public ResponseEntity uploadController(@RequestParam("KumulousApplication") ApplicationType applicationType,
			@RequestPart("file") MultipartFile file) throws IOException, InternalException {
		//	@Parameter(schema = @Schema(type = "string",format = "Binary"))@RequestPart("file") MultipartFile file) throws IOException, InternalException{

		if (file.getOriginalFilename() == null) {

			return new ResponseEntity(HttpStatus.BAD_REQUEST);
		}
		UploadResponse response = fileUploadService.fileUpload(applicationType, file);

		return new ResponseEntity(response, HttpStatus.OK);

	}

	@Operation(description = "API to cancel the scheduled transaction identified by ID", summary = "This will cancel the scheduled transaction")
	@DeleteMapping("/cancelScheduledTransaction")
	public ResponseEntity CancelScheduledNotification(
			@RequestParam("KumulousApplication") ApplicationType applicationType,
			@Parameter(schema = @Schema(type = "string"), name = "id", description = "ID of the scheduled transaction to be cancelled", example = "3", required = true) @RequestParam String id)
			throws IOException {

		CancelScheduledNotificationResponse resp = fileUploadService.cancelScheduledNotification(applicationType, id);

		if (resp.getOutcomeCode() == 204) {

			return new ResponseEntity(resp, HttpStatus.OK);

		}
		
		if (resp.getOutcomeCode() == 404) {
			
			return new ResponseEntity(resp, HttpStatus.NOT_FOUND);
		}
		else {

			return new ResponseEntity(resp, HttpStatus.BAD_REQUEST);
		}

	}

}