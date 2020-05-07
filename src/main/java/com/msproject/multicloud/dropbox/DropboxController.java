package com.msproject.multicloud.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.DeleteResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.msproject.multicloud.FileUtils;
import com.msproject.multicloud.MediaTypeUtils;
import com.msproject.multicloud.Message;
import com.msproject.multicloud.TokenStore;
import com.msproject.multicloud.util.HexCodeUtil;

@Controller
public class DropboxController {
	
	@Value("${file.keep.temp}")
	private String fileTempDir;

	@GetMapping(value = { "/dropboxPage" })
	public String gDriveHomePage() {
		return "dropbox.html";
	}

	@GetMapping(value = { "/dropboxListfiles" }, produces = { "application/json" })
	public @ResponseBody List<DropboxItem> listFiles(HttpServletRequest request)
			throws ListFolderErrorException, DbxException {
		String parentPath = request.getParameter("parentPath");

		TokenStore store = TokenStore.getInstance();
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		DbxClientV2 client = new DbxClientV2(config, store.getDropboxCode());

		ListFolderResult listResult = client.files().listFolder(parentPath);

		List<DropboxItem> responseList = new ArrayList<>();
		for (Metadata data : listResult.getEntries()) {
			DropboxItem item = new DropboxItem();
			item.setName(data.getName());
			item.setParentSharedFolderId(data.getParentSharedFolderId());
			item.setPath(data.getPathDisplay());
			item.setHexcodedPath(HexCodeUtil.hexCodeEncode(data.getPathDisplay()));
			item.setPathLower(data.getPathLower());
			item.setFolder((data instanceof FolderMetadata));
			item.setDownload("");
			responseList.add(item);
		}

		return responseList;
	}

	@DeleteMapping(value = { "/dropboxdeletefile" }, produces = "application/json")
	public @ResponseBody Message deleteFile(HttpServletRequest request) throws Exception {

		String pathToDel = request.getParameter("path");

		TokenStore store = TokenStore.getInstance();
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		DbxClientV2 client = new DbxClientV2(config, store.getDropboxCode());

		DeleteResult delResult = client.files().deleteV2(pathToDel);
		Message message = new Message();
		message.setMessage(delResult.toString());
		return message;
	}

	@PostMapping(value = { "/dropboxcreatefolder/{encPath}" }, produces = { "application/json" })
	public @ResponseBody Message createFolder(@PathVariable("encPath") String encodedPath, HttpServletRequest request)
			throws IOException, CreateFolderErrorException, DbxException {

		String name = request.getParameter("dropboxNewFldr");
		String path = HexCodeUtil.decodeHexString(encodedPath);
		if(path.equals("root")) {
			path = "";
		}

		TokenStore store = TokenStore.getInstance();
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		DbxClientV2 client = new DbxClientV2(config, store.getDropboxCode());
		CreateFolderResult result = client.files().createFolderV2(path + "/" + name);

		Message msg = new Message();
		msg.setMessage(result.toString());
		return msg;
	}

	@PostMapping(value = { "/dropboxuploadfile/{encPath}" }, produces = { "application/json" })
	public @ResponseBody Message createFile(@PathVariable("encPath") String encPath,
			@RequestParam("file") MultipartFile uploadFile) throws IOException, UploadErrorException, DbxException {

		String filename = uploadFile.getOriginalFilename();
		InputStream is = uploadFile.getInputStream();
		String path = HexCodeUtil.decodeHexString(encPath);
		String absPath = new StringBuilder(path).append("/").append(filename).toString();

		TokenStore store = TokenStore.getInstance();
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		DbxClientV2 client = new DbxClientV2(config, store.getDropboxCode());
		FileMetadata data = client.files().uploadBuilder(absPath).uploadAndFinish(is);

		Message msg = new Message();
		msg.setMessage(data.getPathDisplay());
		return msg;
	}

	@GetMapping(value = { "/dropboxdownload/{encodedPath}" })
	public ResponseEntity<Object> downloadFile(@PathVariable("encodedPath") String encodedPath,
			HttpServletResponse response, HttpServletRequest request) throws DbxException, IOException {
		String path = HexCodeUtil.decodeHexString(encodedPath);
		String filename = getFilename(path);

		TokenStore store = TokenStore.getInstance();
		DbxRequestConfig config = new DbxRequestConfig("MultiCloudemo");
		DbxClientV2 client = new DbxClientV2(config, store.getDropboxCode());

		String dpDwnloadFldr = FileUtils.getDropboxDownloadFolder(fileTempDir);
		java.io.File file = new java.io.File(dpDwnloadFldr+File.separator + filename);
		FileOutputStream fos = new FileOutputStream(file);

		client.files().downloadBuilder(path).download(fos);
		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

		MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(request.getServletContext(), file.getName());
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		ResponseEntity<Object> responseEntity = ResponseEntity.ok().headers(headers).contentLength(file.length())
				.contentType(mediaType).body(resource);
		return responseEntity;
	}

	private static String getFilename(String path) {
		String filename = path;
		if (path != null) {
			int ch = path.lastIndexOf("/");
			filename = path.substring(ch + 1);
		}

		return filename;
	}

}
