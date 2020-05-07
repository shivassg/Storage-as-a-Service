package com.msproject.multicloud.dropbox;

import java.io.Serializable;

public class DropboxItem implements Serializable {

	private static final long serialVersionUID = -5301073662990522285L;
	private String name;
	private String parentSharedFolderId;
	private String path;
	private String pathLower;
	private boolean isFolder;
	private String hexcodedPath;
	private String download;

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}

	public String getHexcodedPath() {
		return hexcodedPath;
	}

	public void setHexcodedPath(String hexcodedPath) {
		this.hexcodedPath = hexcodedPath;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentSharedFolderId() {
		return parentSharedFolderId;
	}

	public void setParentSharedFolderId(String parentSharedFolderId) {
		this.parentSharedFolderId = parentSharedFolderId;
	}

	public String getPathLower() {
		return pathLower;
	}

	public void setPathLower(String pathLower) {
		this.pathLower = pathLower;
	}

}
