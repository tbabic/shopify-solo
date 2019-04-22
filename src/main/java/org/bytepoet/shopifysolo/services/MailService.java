package org.bytepoet.shopifysolo.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {
	
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);
	
	@Value("${email.auth.user}")
	private String user;
	
	@Value("${email.auth.password}")
	private String password;
	
	


	public void sendEmail(String to, String subject, String body, List<MailAttachment> attachments) {
		sendEmail(new MailReceipient(to), subject, body, attachments);
	}
	
	public void sendEmail(String to, List<String> cc, List<String> bcc, String subject, String body, List<MailAttachment> attachments) {
		sendEmail(new MailReceipient(to).cc(cc).bcc(bcc), subject, body, attachments);
	}
	
	public void sendEmail(MailReceipient recepient, String subject, String body, List<MailAttachment> attachments) {
		
		
		//Setting up configurations for the email connection to the Google SMTP server using TLS
		Properties props = new Properties();
		props.put("mail.smtp.host", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		//Establishing a session with required user details
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		});
		try {
			MimeMessage msg = new MimeMessage(session);
			InternetAddress[] address = InternetAddress.parse(recepient.to, true);
			msg.setRecipients(Message.RecipientType.TO, address);
			if (!recepient.bcc.isEmpty()) {
				InternetAddress[] bcc = InternetAddress.parse(StringUtils.join(recepient.bcc, ","), true);
				msg.setRecipients(Message.RecipientType.BCC, bcc);
			}
			msg.setSubject(subject, "UTF-8");
			

			Multipart multipart = new MimeMultipart();
			BodyPart messageContent = new MimeBodyPart();
			messageContent.setContent(body, "text/plain; charset=UTF-8");
			
			
			multipart.addBodyPart(messageContent);
			
			for (MailAttachment attachment : attachments) {
				BodyPart messageBodyPart = new MimeBodyPart();
				String filename = attachment.filename;
				DataSource source = new ByteArrayDataSource(attachment.content, attachment.mimeType);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(filename);
				multipart.addBodyPart(messageBodyPart);
			}
			
			msg.setContent(multipart);
			
			Transport.send(msg);
			logger.info("Mail has been sent successfully");
		} catch (MessagingException | IOException mex) {
			throw new RuntimeException(mex);
		}
	}
	
	
	
	
	
	
	public static class MailReceipient {
		
		private String to;
		private List<String> cc = new ArrayList<>();
		private List<String> bcc = new ArrayList<>();
		
		public MailReceipient(String to) {
			super();
			this.to = to;
		}
		
		public MailReceipient cc(String cc) {
			this.cc.add(cc);
			return this;
		}
		
		public MailReceipient cc(List<String> cc) {
			this.cc.addAll(cc);
			return this;
		}
		
		public MailReceipient bcc(String bcc) {
			this.bcc.add(bcc);
			return this;
		}
		
		public MailReceipient bcc(List<String> bcc) {
			this.bcc.addAll(bcc);
			return this;
		}
		
	}
	
	public static class MailAttachment {
		
		private String filename;
		private String mimeType;
		private InputStream content;
		
		public MailAttachment filename(String filename) {
			this.filename = filename;
			return this;
		}
		public MailAttachment mimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}
		public MailAttachment content(InputStream content) {
			this.content = content;
			return this;
		}
		
		
		
	}
	
}
