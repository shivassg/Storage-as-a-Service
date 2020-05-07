package com.msproject.multicloud;

import java.io.File;

public class FileUtils {

	private FileUtils() {
	}

	public static String getGDriveUploadFolder(String tempDir) {
		if (tempDir != null) {
			tempDir += File.separator + "gdrive" + File.separator + "upload";
			if (!new File(tempDir).exists()) {
				new File(tempDir).mkdirs();
			}
		}

		return tempDir;
	}

	public static String getGDriveDownloadFolder(String tempDir) {
		if (tempDir != null) {
			tempDir += File.separator + "gdrive" + File.separator + "download";
			if (!new File(tempDir).exists()) {
				new File(tempDir).mkdirs();
			}
		}

		return tempDir;
	}

	public static String getDropboxUploadFolder(String tempDir) {
		if (tempDir != null) {
			tempDir += File.separator + "dropbox" + File.separator + "upload";
			if (!new File(tempDir).exists()) {
				new File(tempDir).mkdirs();
			}
		}

		return tempDir;
	}

	public static String getDropboxDownloadFolder(String tempDir) {
		if (tempDir != null) {
			tempDir += File.separator + "dropbox" + File.separator + "download";
			if (!new File(tempDir).exists()) {
				new File(tempDir).mkdirs();
			}
		}

		return tempDir;
	}

}
