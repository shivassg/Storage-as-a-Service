package com.msproject.multicloud.dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.msproject.multicloud.MediaTypeUtils;

@RestController
public class DownloadController {
	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ResponseEntity<Object> downloadFile(HttpServletRequest request) throws IOException {
		String filename = "D:/work/tree.jpg";
		File file = new File(filename);
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
