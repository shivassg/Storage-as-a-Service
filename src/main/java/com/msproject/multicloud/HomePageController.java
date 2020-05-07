package com.msproject.multicloud;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxSessionStore;
import com.dropbox.core.DbxStandardSessionStore;
import com.dropbox.core.DbxWebAuth;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.msproject.multicloud.googledrive.GoogleFlow;

@Controller
public class HomePageController {
	
	@Autowired
	private GoogleFlow flowStore;
	
	private DbxWebAuth webAuth;
	
	private static final String USER_IDENTIFIER_KEY = "MY_DUMMY_USER";
	
	@Value("${google.oauth.callback.uri}")
	private String CALLBACK_URI;
	
	@Value("${dropbox.key}")
	private String dropBoxKey;
	
	@Value("${dropbox.secret}")
	private String dropBoxSecret;
	
	@Value("${dropbox.redirectUri}")
	private String dropBoxRedirectUri;
	
	
	@GetMapping(value = {"/"})
	public String showHomePage() {
		return "home.html";
	}
	
	@GetMapping(value = { "/googlesignin" })
	public void doGoogleSignIn(HttpServletResponse response) throws Exception {
		GoogleAuthorizationCodeRequestUrl url = flowStore.getFlow().newAuthorizationUrl();
		String redirectURL = url.setRedirectUri(CALLBACK_URI).setAccessType("offline").build();
		response.sendRedirect(redirectURL);
	}
	
	@GetMapping(value = { "/dropboxsignin" })
	public void doDropboxSignIn(HttpSession session, HttpServletResponse response) throws Exception {
		DbxAppInfo appInfo = new DbxAppInfo(dropBoxKey, dropBoxSecret);
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		
		DbxSessionStore sessionStore = new DbxStandardSessionStore(session, dropBoxKey);
		DbxWebAuth.Request authRequest = DbxWebAuth.newRequestBuilder().withRedirectUri(dropBoxRedirectUri, sessionStore).build();
		webAuth = new DbxWebAuth(config, appInfo);
		response.sendRedirect(webAuth.authorize(authRequest));
	}
	
	@GetMapping(value = "/codes",produces = {"application/json"})
	public @ResponseBody Codes getCodes() {
		TokenStore store = TokenStore.getInstance();
		Codes codes = new Codes();
		codes.setGoogleCode(store.hasGooleCode());
		codes.setDropboxCode(store.hasDropboxCode());
		return codes;
	}
	
	@GetMapping(value= {"/oauthcomplete"})
	public String saveDropboxAuthorizationCode(HttpServletRequest request) throws DbxException {
		String code = request.getParameter("code");
		if(code != null) {
			DbxAuthFinish authFinish = webAuth.finishFromCode(code, dropBoxRedirectUri);
			TokenStore store = TokenStore.getInstance();
			store.addDropboxCode(authFinish.getAccessToken());
		}
		return "home.html";
	}
	
	@GetMapping(value = { "/oauth" })
	public String saveAuthorizationCode(HttpServletRequest request) throws Exception {
		String code = request.getParameter("code");
		if (code != null) {
			saveToken(code);
			TokenStore store = TokenStore.getInstance();
			store.addGoogleCode(code);

			return "home.html";
		}

		return "home.html";
	}

	private void saveToken(String code) throws Exception {
		GoogleTokenResponse response = flowStore.getFlow().newTokenRequest(code).setRedirectUri(CALLBACK_URI).execute();
		flowStore.getFlow().createAndStoreCredential(response, USER_IDENTIFIER_KEY);
	}

}
