package com.msproject.multicloud.googledrive;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.msproject.multicloud.Constants;
import com.msproject.multicloud.FileUtils;
import com.msproject.multicloud.MediaTypeUtils;
import com.msproject.multicloud.Message;

@Controller
public class GoogleDriveController {

	@Autowired
	private GoogleFlow flowStore;

	@Value("${file.keep.temp}")
	private String fileTempDir;

	@GetMapping(value = { "/gdrivePage" })
	public String gDriveHomePage() {
		return "gdrive.html";
	}

	@DeleteMapping(value = { "/gdrivedeletefile/{fileId}" }, produces = "application/json")
	public @ResponseBody Message deleteFile(@PathVariable(name = "fileId") String fileId) throws Exception {

		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive drive = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();
		drive.files().delete(fileId).execute();

		Message message = new Message();
		message.setMessage("success");
		return message;
	}

	@GetMapping(value = { "/gDriveListfiles/{parentId}" }, produces = { "application/json" })
	public @ResponseBody List<GoogleDriveFileItem> listFiles(@PathVariable(name = "parentId") String parentId)
			throws Exception {

		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive drive = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();

		List<GoogleDriveFileItem> responseList = new ArrayList<>();
		com.google.api.services.drive.Drive.Files.List list = drive.files().list();
		if (!"0".equals(parentId)) {
			String qry = String.format("'%s' in parents", parentId);
			list = list.setQ(qry);
		} else {
			list = list.setQ("'root' in parents");
		}

		FileList fileList = list.setFields("files(id,name,thumbnailLink,capabilities)").execute();
		for (File file : fileList.getFiles()) {
			GoogleDriveFileItem item = new GoogleDriveFileItem();
			item.setId(file.getId());
			item.setName(file.getName());
			item.setThumbnailLink(file.getThumbnailLink());
			item.setDownloadUrl(file.getWebContentLink());
			if (file.getCapabilities() != null) {
				item.setIsFolder(file.getCapabilities().getCanAddChildren());
			}

			responseList.add(item);
		}

		return responseList;
	}

	@PostMapping(value = { "/gdrivecreatefolder/{folderId}" }, produces = { "application/json" })
	public @ResponseBody Message createFolder(@PathVariable("folderId") String folderId, HttpServletRequest request)
			throws IOException {

		String name = request.getParameter("gDriveNewFldr");

		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive drive = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();

		File file = new File();
		file.setName(name);
		file.setMimeType("application/vnd.google-apps.folder");
		if (!"0".equals(folderId)) {
			file.setParents(Collections.singletonList(folderId));
		} else {
			file.setParents(Collections.singletonList("root"));
		}
		drive.files().create(file).execute();

		Message msg = new Message();
		msg.setMessage("success");
		return msg;
	}

	@PostMapping(value = { "/gdriveuploadfile/{folderId}" }, produces = { "application/json" })
	public @ResponseBody Message createFile(@PathVariable("folderId") String folderId,
			@RequestParam("file") MultipartFile uploadFile) throws IOException {

		String filename = uploadFile.getOriginalFilename();
		String gDriveUploadFldr = FileUtils.getGDriveUploadFolder(fileTempDir);
		java.io.File toFile = new java.io.File(gDriveUploadFldr + java.io.File.separator + filename);
		FileOutputStream fos = new FileOutputStream(toFile);
		InputStream is = uploadFile.getInputStream();

		byte[] buffer = new byte[1024];
		int length;
		while ((length = is.read(buffer)) > 0) {
			fos.write(buffer, 0, length);
		}

		try {
			fos.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive drive = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();

		File file = new File();
		file.setName(filename);
		if (!"0".equals(folderId)) {
			file.setParents(Collections.singletonList(folderId));
		} else {
			file.setParents(Collections.singletonList("root"));
		}

		FileContent content = new FileContent("image/jpeg", toFile);
		File uploadedFile = drive.files().create(file, content).setFields("id").execute();

		Message msg = new Message();
		msg.setMessage(uploadedFile.getId());
		return msg;
	}

	@GetMapping(value = { "/test/{folderId}" }, produces = { "application/json" })
	public @ResponseBody Message getPath(@PathVariable("folderId") String fileId) throws IOException {

		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive drive = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();

		StringBuilder sb = new StringBuilder();

		File file = drive.files().get(fileId).setFields("name, parents").execute();
		while (file.getParents() != null && !file.getParents().isEmpty()) {
			List<String> parents = file.getParents();
			String parentId = parents.get(0);
			sb.append(file.getName()).append(" , ");
			file = drive.files().get(parentId).setFields("name, parents").execute();

		}
		Message msg = new Message();
		msg.setMessage(sb.toString());
		return msg;
	}
	
	@GetMapping(value = { "/gdrivedownload/{fileId}" })
	public ResponseEntity<Object> downloadFile(@PathVariable("fileId") String fileId,
			HttpServletResponse response, HttpServletRequest request) throws IOException{
		
		Credential cred = flowStore.getFlow().loadCredential(Constants.USER_IDENTIFIER_KEY);
		Drive client = new Drive.Builder(Constants.HTTP_TRANSPORT, Constants.JSON_FACTORY, cred)
				.setApplicationName("MultiCloudemo").build();
		File gfile = client.files().get(fileId).execute();
		

		String dpDwnloadFldr = FileUtils.getGDriveDownloadFolder(fileTempDir);
		java.io.File file = new java.io.File(dpDwnloadFldr+ java.io.File.separator + gfile.getName());
		FileOutputStream fos = new FileOutputStream(file);

		client.files().get(fileId).executeMediaAndDownloadTo(fos);
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

}
