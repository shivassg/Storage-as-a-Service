package com.msproject.multicloud;

import java.io.Serializable;

public class Codes implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private boolean googleCode;
	private boolean dropboxCode;

	public boolean isGoogleCode() {
		return googleCode;
	}

	public void setGoogleCode(boolean googleCode) {
		this.googleCode = googleCode;
	}

	public boolean isDropboxCode() {
		return dropboxCode;
	}

	public void setDropboxCode(boolean dropboxCode) {
		this.dropboxCode = dropboxCode;
	}

}
