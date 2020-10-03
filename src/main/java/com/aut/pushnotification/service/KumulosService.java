package com.aut.pushnotification.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.aut.pushnotification.exception.InternalException;
import com.aut.pushnotification.model.ApplicationType;
import com.aut.pushnotification.model.CancelScheduledNotificationResponse;
import com.aut.pushnotification.model.ChannelResponseErrorObj;
import com.aut.pushnotification.model.ChannelResponseObj;
import com.aut.pushnotification.model.NotifiRequest;
import com.aut.pushnotification.model.NotificationResponseObj;
import com.aut.pushnotification.model.UploadResponse;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class KumulosService {

	@Value("${app.apiKey}")
	private String apiKey;

	@Value("${app.serverKey}")
	private String serverKey;	

	@Value("${app.bulkuploadurl}")
	private String bulkuploadurl;

	@Value("${app.channelUrl}")
	private String channelUrl;

	@Value("${app.cancelScheduledNotificationUrl}")
	private String cancelScheduledNotificationUrl;

	@Value("${app.filepath}")
	private String filepath;

	OkHttpClient client = new OkHttpClient();

	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
	Date date = new Date(System.currentTimeMillis());

	public UploadResponse fetchResponse(ApplicationType applicationType, File f) throws IOException, InternalException {

		log.info("Entered into the kumulous CSV processing method and the time now is" + " " + formatter.format(date));

		File file = new File(f.getAbsolutePath());
		String fileName = file.getName();

		if (fileName == null || fileName.isEmpty()) {
			throw new InternalException(200, "Filename is empty", "filename cannot be fetched from the csv file");

		}
//String channelName = fileName.substring(0, fileName.length() - 4);
		String channelName = FilenameUtils.getBaseName(fileName);

		ChannelResponseObj channelResponse = createChannel(applicationType, channelName);

		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("users", file.getName(), RequestBody.create(MediaType.parse("text/csv"), file))
				// .addFormDataPart("some-field", "some-value")
				.build();

		String credentials = Credentials.basic(apiKey, serverKey);
		
		// Request request = new
		// Request.Builder().url("https://messages.kumulos.com/v2/notifications").addHeader("Authorization",
		// "Basic
		// YWZjOWI3OWUtZGMyMy00NjNlLWJiYTktODMzMmUzMGMzZmZmOnBlSEtCTGFkRmpsbUdscEt0QzNZQjJZcUZzdFdScEZXOUZtUg==").addHeader("Accept",
		// "application/json")
		Request request = new Request.Builder().url(bulkuploadurl + channelName + "/subscribers-file")
				.addHeader("Authorization", credentials).addHeader("Accept", "application/json").post(requestBody)
				.build();

		try (Response response = client.newCall(request).execute()) {
			log.info("result code  : " + " " + response.code());
			// log.info("result : " + " " + response.body().string());
			UploadResponse uploadresponse = new UploadResponse();

			if (response.code() == 202) {
				uploadresponse.setUploadOutcomeCode(response.code());
				uploadresponse.setUploadOutcomeMessage("Upload Success");
				uploadresponse.setUploadInternalMessage("Uploaded the CSV to the channel" + " " + channelName);
				uploadresponse.setFileName(channelName + ".csv");
				String title = "Hi AUT user";
				String message = "Hello how are you, ";
				String sendAt = "";
				String strategy = "";
				String pastTimes = " ";
				boolean scheduled = false;

				NotifiRequest notireq = new NotifiRequest();
				notireq.setMessage(message);
				notireq.setChannelName(channelName);
				notireq.setPastTimes(pastTimes);
				notireq.setSendAt(sendAt);
				notireq.setStrategy(strategy);
				notireq.setTitle(title);
				notireq.setScheduled(scheduled);

				NotificationResponseObj Notifyobj = processChannelNotification(applicationType, notireq);
				uploadresponse.setNotificationOutcomeCode(Notifyobj.getNotificationOutcomeCode());
				uploadresponse.setNotificationInternalMessage(Notifyobj.getNotificationInternalMessage());
				uploadresponse.setNotificationOutcomeMessage(Notifyobj.getNotificationOutcomeMessage());

				uploadresponse.setChannelCreationOutcomeCode(channelResponse.getOutcomeCode());
				uploadresponse.setChannelCreationInternalMessage(channelResponse.getInternalMessage());
				uploadresponse.setChannelCreationOutcomeMessage(channelResponse.getOutcomeMessage());

				uploadresponse.setOutcomeCode(Notifyobj.getNotificationOutcomeCode());
				uploadresponse.setInternalMessage(
						"Users are uploaded to the channel ( channel name is identical to the filename), and then push notification is sent to the end users");
				uploadresponse.setOutcomeMessage(Notifyobj.getNotificationOutcomeMessage());
			}

			else {
				uploadresponse.setOutcomeCode(response.code());
				uploadresponse.setOutcomeMessage("Upload Failed with Kumulous error : " + response.body().string());
				uploadresponse.setInternalMessage("Failure");
				uploadresponse.setFileName(fileName);
			}
			return uploadresponse;

		}

	}

	// @Scheduled(cron = "0 35 12 * * *")
	public List<UploadResponse> fileUpload(ApplicationType applicationType) throws IOException, InternalException {

		File dir = new File(filepath);

		FileFilter fileFilter = new WildcardFileFilter("*.csv");
		File[] files = dir.listFiles(fileFilter);
		int numberoffiles = files.length;
		log.info("The length of the files are" + " " + numberoffiles);
		List<UploadResponse> responseArray = new ArrayList<UploadResponse>();

		UploadResponse response = new UploadResponse();
		for (int i = 0; i <= numberoffiles - 1; i++) {
			log.info("The file name to be processed is  " + " " + files[i]);
			response = fetchResponse(applicationType, files[i]);
			responseArray.add(i, response);
		}

		return responseArray;

	}

	public UploadResponse fileUpload(ApplicationType applicationType, MultipartFile file)
			throws IOException, InternalException {

		String fileExtension = getFileNamExtension(file.getOriginalFilename());
		log.info("The extension of the file is " + " " + fileExtension);
		if (!fileExtension.equalsIgnoreCase("csv")) {

			log.error("The extension of the file is NOT CSV, but it is " + " "
					+ getFileNamExtension(file.getOriginalFilename()));

			throw new InternalException(400, "file upload failed", "File extension is not in CSV format");

		}

		// byte[] bytes = file.getBytes();
		Path currentDir = Paths.get(".");
		Path fullPath = currentDir.toAbsolutePath();
		log.info("currentDir abosulte path is " + " " + fullPath.toString());

		// code to write the file inside the current folder with the existing filename.
		Path path = Paths.get(file.getOriginalFilename());
		// Files.write(path, bytes);
		log.info("Filename of the CSV is " + path.getFileName());

		UploadResponse response = new UploadResponse();
		// File f2 = convert(file);

		File f2 = File.createTempFile(path.getFileName().toString(), fileExtension);
		file.transferTo(f2);

		response = fetchResponse(applicationType, f2);
		// delete immediate
		boolean deletedTempFile = f2.delete();
		log.info("The temp file is succesfully deleted: ? : " + deletedTempFile);
		// List<UploadResponse> responseArray = new ArrayList<UploadResponse>();

		return response;

	}

	public static File convert(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		convFile.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(convFile);) {
			log.info("File is converted from multipart file to a regular file");
			fos.write(file.getBytes());
			// fos.close();
		}
		return convFile;
	}

	public String getFileNamExtension(String fileName) {

		return fileName.substring(fileName.lastIndexOf('.') + 1);
	}

	public ChannelResponseObj createChannel(ApplicationType applicationType, String ChannelUUID)
			throws IOException, InternalException {

		log.info("Entered into the Create Channel method with Channel Name" + " " + ChannelUUID + " "
				+ "and the time now is" + " " + formatter.format(date));

		String json = "{\r\n" + "\"uuid\": \"" + ChannelUUID + "\",\r\n" + "\"name\": \"" + ChannelUUID
				+ "\",\r\n" + "\"showInPortal\": true \r\n" + " }\r\n" + "\r\n" + "";

		/*
		 * String json = "{\r\n" + "      \"uuid\": \"" + ChannelUUID + "\",\r\n" +
		 * "      \"name\": \"" + ChannelUUID + "\",\r\n" +
		 * "      \"showInPortal\": true,\r\n" + "      \"meta\": {\r\n" +
		 * "          \"Company info\": \"Tranxactor\"\r\n" + "      }\r\n" + "}\r\n" +
		 * "\r\n" + "";
		 */
		String credentials = Credentials.basic(apiKey, serverKey);
		RequestBody body =   RequestBody.create(MediaType.parse("application/json"), json);

		Request request = new Request.Builder().url(channelUrl).addHeader("Authorization", credentials)
				.addHeader("Accept", "application/json").addHeader("Content-Type", "application/json").post(body).build();
		

		ChannelResponseObj channelResponse = new ChannelResponseObj();

		try (Response response = client.newCall(request).execute()) {

			if (response.code() == 201) {
				ChannelResponseObj jsonObject = new Gson().fromJson(response.body().string(), ChannelResponseObj.class);
				String id = jsonObject.getId();
				String showInPortal = jsonObject.getShowInPortal();
				String createdAt = jsonObject.getCreatedAt();
				String updatedAt = jsonObject.getUpdatedAt();
				String uuid = jsonObject.getUuid();
				String friendlyName = jsonObject.getName();
				// String channelInfo= jsonObject.getMeta().getChannelInfo();
				channelResponse.setOutcomeCode(response.code());
				channelResponse.setOutcomeMessage("Channel creation Success");
				channelResponse.setInternalMessage("Success");
				channelResponse.setId(id);
				channelResponse.setCreatedAt(createdAt);
				channelResponse.setUpdatedAt(updatedAt);
				channelResponse.setUuid(uuid);
				channelResponse.setName(friendlyName);
				channelResponse.setShowInPortal(showInPortal);
				log.info("Channel with UUID : " + " " + uuid + " " + "having friendly name : " + " " + friendlyName
						+ " " + "with id" + " " + id + " " + "is created.");
			}

			else {
				String responseBody = response.body().string();
				ChannelResponseErrorObj jsonObject = new Gson().fromJson(responseBody,
						ChannelResponseErrorObj.class);
				String error = jsonObject.getError();
				channelResponse.setOutcomeCode(response.code());
				channelResponse.setOutcomeMessage("Channel name already exists");
				channelResponse.setInternalMessage("Channel name exists error from Kumulos : " + " " + error);
				log.info("Error :" + " " + error);
				if (!(error.equalsIgnoreCase("uuid must be unique"))) {

					throw new InternalException(response.code(), "Channel creation failed",
							"Failure : " + " " + responseBody);
				}

			}
			return channelResponse;

		}

	}

	public NotificationResponseObj processChannelNotification(ApplicationType applicationType, NotifiRequest notireq)
			throws IOException {

		String UUID = notireq.getChannelName();
		String title = notireq.getTitle();
		String message = notireq.getMessage();
		boolean scheduled = notireq.isScheduled();
		String sendAt = notireq.getSendAt();
		String strategy = notireq.getStrategy();
		String pastTimes = notireq.getPastTimes();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		String json = "";

		if (scheduled) {

			json = "{\r\n" + "\"target\": {\r\n" + "\"broadcast\": false,\r\n"
					+ "\"channelUuids\": [\"" + UUID + "\"\n" + "]\n" + "},\r\n"
					+ "\"content\": {\r\n" + "\"title\": \"" + title + "\",\r\n"
					+ "\"message\": \"" + message + " " + "this push notification is sent to you on " + " "
					+ formatter.format(date) + " " + "via the channel name" + " " + UUID + " to be sent on" + " "
					+ sendAt + "\"\r\n" + "},\r\n" + "\"runBackgroundHandler\": true,\r\n"+ "\"scheduled\": true,\r\n" + "\"schedule\": {\r\n"
					+ "\"sendAt\": \"" + sendAt + "\",\r\n" + "\"strategy\": \"" + strategy + "\",\r\n"
					+ "\"pastTimes\": \"" + pastTimes + "\"\r\n" + "}\r\n" + "}";

		}

		else {

			json = "{\n" + "\"target\": {\n" + "\"broadcast\": false,\n" + "\"channelUuids\": [\""
					+ UUID + "\"\n" + "]\n" + "},\n" +"\"runBackgroundHandler\": true,\r\n"+ "\"content\": {\n" + "\"title\": \""
					+ title + "\",\n" + "\"message\": \"" + message + " "
					+ "this push notification is sent to you on " + " " + formatter.format(date) + " "
					+ "via the channel name" + " " + UUID + "\"\n" + "}\n" + "}";

		}

		log.info("json  ::" + json);

		RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
		String credentials = Credentials.basic(apiKey, serverKey);
		Request request = new Request.Builder().url("https://messages.kumulos.com/v2/notifications")
				.header("Authorization", credentials).addHeader("Accept", "application/json")

				.post(body).build();

		try (Response response = client.newCall(request).execute()) {
			log.info("result code  : " + response.code());

			NotificationResponseObj resp = new Gson().fromJson(response.body().string(), NotificationResponseObj.class);

			if (response.code() == 202) {
				resp.setNotificationOutcomeCode(response.code());
				resp.setNotificationInternalMessage("Notification sent Successfully");
				resp.setNotificationOutcomeMessage("Success");

				resp.setOutcomeCode(response.code());
				resp.setInternalMessage("Notification sent Successfully");
				resp.setOutcomeMessage("Success");

				return resp;

			}

			else {

				resp.setNotificationOutcomeCode(response.code());
				resp.setNotificationInternalMessage("Notification send failed");

				return resp;

			}
		}

	}

	public CancelScheduledNotificationResponse cancelScheduledNotification(ApplicationType applicationType, String id)
			throws IOException {

		String credentials = Credentials.basic(apiKey, serverKey);
		Request request = new Request.Builder().url(cancelScheduledNotificationUrl + id)
				.header("Authorization", credentials).addHeader("Accept", "application/json").delete().build();
		log.info("Request URL is : " + request.toString());

		try (Response response = client.newCall(request).execute()) {

			log.info("result code : " + response.code());

			// log.info("result code : " + cancelResponse.body().string());
			// No Response Body for deletion when success.
			// CancelScheduledNotificationResponse resp = new
			// Gson().fromJson(response.body().string(),
			// CancelScheduledNotificationResponse.class);
			CancelScheduledNotificationResponse resp = new CancelScheduledNotificationResponse();

			if (response.code() == 204) {
				resp.setOutcomeCode(response.code());
				resp.setOutcomeMessage("Succesfully deleted the scheduled transaction");
				resp.setInternalMessage("Success");
			}

			if (response.code() == 400) {
				resp = new Gson().fromJson(response.body().string(), CancelScheduledNotificationResponse.class);
				resp.setOutcomeCode(response.code());
				resp.setInternalMessage("Error from Kumulous : " + resp.getError());
				resp.setOutcomeMessage("Failed to delete the scheduled notification since its already sent or deleted");
				

			}
			
			if(response.code() == 404) {
				//String responseBody = response.body().string();
				//resp = new Gson().fromJson(responseBody, CancelScheduledNotificationResponse.class);
				resp.setOutcomeCode(response.code());
				resp.setOutcomeMessage("A notification with the given ID could not be found");
				resp.setInternalMessage("Error from Kumulous : " + "A notification with the given ID could not be found");
			}

			return resp;
		}
	}

}