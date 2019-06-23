package org.bytepoet.shopifysolo.services;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.services.MailService.MailAttachment;
import org.bytepoet.shopifysolo.services.MailService.MailReceipient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class SoloMaillingService {

	
	@Autowired
	private MailService mailService;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	
	public void sendEmailWithPdf(String email, String pdfUrl, String subject, String body) {
		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(pdfUrl).get().build();
		try {
			Response response = client.newCall(request).execute();
			String fileName = extractFileNameFromContentDisposition(response.header("Content-Disposition"));
			MailAttachment attachment = new MailAttachment()
					.filename(fileName)
					.mimeType("application/pdf")
					.content(response.body().byteStream());		
			
			MailReceipient to = new MailReceipient(email);
			if (StringUtils.isNotBlank(alwaysBcc)) {
				to.bcc(alwaysBcc);
			}
			mailService.sendEmail(to, subject, body, Arrays.asList(attachment));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private String extractFileNameFromContentDisposition(String contentDispositionHeader) {
		return contentDispositionHeader.replaceFirst("attachment;filename=", "").replace("\"", "");
	}
}
