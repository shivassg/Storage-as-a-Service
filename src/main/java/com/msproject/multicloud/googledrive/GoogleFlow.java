package com.msproject.multicloud.googledrive;

import java.io.InputStreamReader;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.msproject.multicloud.Constants;

@Component
public class GoogleFlow {

	@Value("${google.secret.key.path}")
	private Resource gdSecretKeys;

	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;

	@Value("${google.credentials.folder.path}")
	private Resource credentialsFolder;

	@Value("${google.service.account.key}")
	private Resource serviceAccountKey;

	private GoogleAuthorizationCodeFlow flow;

	public GoogleAuthorizationCodeFlow getFlow() {
		return flow;
	}

	public void setFlow(GoogleAuthorizationCodeFlow flow) {
		this.flow = flow;
	}

	@PostConstruct
	public void init() throws Exception {
		GoogleClientSecrets secrets = GoogleClientSecrets.load(Constants.JSON_FACTORY,
				new InputStreamReader(gdSecretKeys.getInputStream()));
		flow = new GoogleAuthorizationCodeFlow.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, secrets,
				Constants.SCOPES).setDataStoreFactory(new FileDataStoreFactory(credentialsFolder.getFile())).build();
	}

}
