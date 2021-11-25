package org.bytepoet.shopifysolo.epk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.epk.model.EpkBook;
import org.bytepoet.shopifysolo.epk.model.EpkCustomsData;
import org.bytepoet.shopifysolo.epk.model.EpkCustomsNonEuAdditionalData;
import org.bytepoet.shopifysolo.epk.model.EpkExpressMail;
import org.bytepoet.shopifysolo.epk.model.EpkFooter;
import org.bytepoet.shopifysolo.epk.model.EpkHeader;
import org.bytepoet.shopifysolo.epk.model.EpkMailable;
import org.bytepoet.shopifysolo.epk.model.EpkRegisteredMail;
import org.bytepoet.shopifysolo.manager.models.Address;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.services.IsoCountriesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableSet;

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
		int additionalRows = 0;
		for (Order order : orders) {
			try {
				EpkMailable epkMail = null;
				if (StringUtils.isBlank(order.getTrackingNumber())) {
					throw new RuntimeException("Order " + order.getShopifyOrderId() + " does not have tracking number");
				}
				if (order.getWeight() == 0) {
					throw new RuntimeException("Order " + order.getShopifyOrderId() + "does not have weight");
				}
 				if (order.getTrackingNumber().toUpperCase().startsWith("EM")) {
					epkMail = EpkExpressMail.createRow(order.getTrackingNumber(), order);
				} else if (order.getTrackingNumber().toUpperCase().startsWith("RF")) {
					epkMail = EpkRegisteredMail.createRow(order.getTrackingNumber().toUpperCase(), order);
				}
				
				addNewLine(stream);
				addToStream(stream, epkMail.getData());
				
				//if abroad
				Address address = order.getShippingInfo();
				String countryCode = IsoCountriesService.getCountryCode(address.getCountry());
				boolean isCroatia = countryCode.equalsIgnoreCase("HR");
				
				Set<String> euCountryCodes = ImmutableSet.of("AT", "BE", "BG", "CY", "CZ", "DK", "EE", "FI",
						"FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL",
						"PT", "RO", "SK", "SI", "ES", "SE");
				if (!isCroatia) {
					EpkMailable customsData = EpkCustomsData.createRow(order.getTrackingNumber().toUpperCase(), order);
					addNewLine(stream);
					addToStream(stream, customsData.getData());
					additionalRows++;
					if (!euCountryCodes.contains(countryCode)) {
						
						EpkMailable additionalCustomsData = EpkCustomsNonEuAdditionalData.createRow(order.getTrackingNumber().toUpperCase(), order);
						addNewLine(stream);
						addToStream(stream, additionalCustomsData.getData());
						additionalRows++;
					}
				}
				
			} catch (Exception e) {
				throw new RuntimeException("Order id:" + order.getId()+ e.getMessage(), e);
			}
		}
		
		EpkFooter footer = EpkFooter.createFooter(epkUserCode, epkUserName, fileName, epkZipCode, orders.size()+2+additionalRows, orders.size());
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
