package com.msproject.multicloud;

import java.util.Arrays;
import java.util.List;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

public class Constants {
	
	private Constants() {}
	
	public static HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	public static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";
	public static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE,
			"https://www.googleapis.com/auth/drive.install");
	

}
