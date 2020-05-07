package com.msproject.multicloud;

import java.util.HashMap;
import java.util.Map;

public class TokenStore {
	
	private Map<String, String> map = new HashMap<>();
	
	private TokenStore() {}
	
	private static TokenStore store = null;
	
	public static TokenStore getInstance() {
		if(store == null) {
			store = new TokenStore();
		}
		return store;
	}
	
	public void addGoogleCode(String code) {
		map.put("g-code", code);
	}
	
	public String getGoogleCode() {
		return map.get("g-code");
	}
	
	public void addDropboxCode(String code) {
		map.put("db-code", code);
	}
	
	public String getDropboxCode() {
		return map.get("db-code");
	}
	
	public boolean hasGooleCode() {
		return map.containsKey("g-code");
	}
	
	public boolean hasDropboxCode() {
		return map.containsKey("db-code");
	}

}
