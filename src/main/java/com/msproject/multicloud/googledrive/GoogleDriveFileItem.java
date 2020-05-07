package com.msproject.multicloud.googledrive;

import java.io.Serializable;

public class GoogleDriveFileItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String id;

	private String thumbnailLink;

	private boolean isFolder;

	private String downloadUrl;

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setIsFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getThumbnailLink() {
		return thumbnailLink;
	}

	public void setThumbnailLink(String thumbnailLink) {
		this.thumbnailLink = thumbnailLink;
	}

}
