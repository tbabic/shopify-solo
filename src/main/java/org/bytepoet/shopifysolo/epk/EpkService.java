package org.bytepoet.shopifysolo.epk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.epk.model.EpkBook;
import org.bytepoet.shopifysolo.epk.model.EpkExpressMail;
import org.bytepoet.shopifysolo.epk.model.EpkFooter;
import org.bytepoet.shopifysolo.epk.model.EpkHeader;
import org.bytepoet.shopifysolo.epk.model.EpkMailable;
import org.bytepoet.shopifysolo.epk.model.EpkRegisteredMail;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EpkService {

	private static final String FILE_NAME_FORMAT = "{0}.{1}.{2}.001";
	
	@Value("${epk.user.code}")
	private String epkUserCode;
	
	@Value("${epk.user.name}")
	private String epkUserName;
	
	@Value("${epk.zip-code}")
	private String epkZipCode;
	
	
	public EpkBook generateEpk(List<Order> orders) {
		String fileName = generateFileName();
		EpkHeader header = EpkHeader.createHeader(epkUserCode, epkUserName, fileName, epkZipCode);
		ByteArrayOutputStream stream = addToStream(new ByteArrayOutputStream(), header.getData());
		for (Order order : orders) {
			try {
				EpkMailable epkMail = null;
				if (StringUtils.isBlank(order.getTrackingNumber())) {
					throw new RuntimeException("Order " + order.getId() + " does not have tracking number");
				}
				if (order.getTrackingNumber().toUpperCase().startsWith("EM")) {
					epkMail = EpkExpressMail.createRow(order.getTrackingNumber(), order);
				} else if (order.getTrackingNumber().toUpperCase().startsWith("RF")) {
					epkMail = EpkRegisteredMail.createRow(order.getTrackingNumber().toUpperCase(), order);
				}
				
				addNewLine(stream);
				addToStream(stream, epkMail.getData());
			} catch (Exception e) {
				throw new RuntimeException("Order id:" + order.getId()+ e.getMessage(), e);
			}
		}
		
		EpkFooter footer = EpkFooter.createFooter(epkUserCode, epkUserName, fileName, epkZipCode, orders.size()+2, orders.size());
		addNewLine(stream);
		addToStream(stream, footer.getData());
		String base64Value = Base64.encodeBase64String(stream.toByteArray());
		return new EpkBook(fileName+ ".epk", base64Value);
	}
	
	private String generateFileName() {
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		return MessageFormat.format(FILE_NAME_FORMAT, epkZipCode, epkUserCode, df.format(date));
	}
	
	private ByteArrayOutputStream addToStream(ByteArrayOutputStream outputStream, char[] data) {
		try {
			outputStream.write(new String(data).getBytes("windows-1250"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return outputStream;
	}
	
	private ByteArrayOutputStream addNewLine(ByteArrayOutputStream outputStream) {
		return addToStream(outputStream, "\r\n".toCharArray());
	}
}
