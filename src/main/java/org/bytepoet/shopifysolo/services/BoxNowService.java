package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.CsvBuilder;
import org.bytepoet.shopifysolo.manager.models.FileData;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BoxNowService {
	
	@Value("${boxnow.location}")
	private String location;

	
	public FileData generateAddressBookCsv(List<Order> orders) {
		
		byte [] bytes = new CsvBuilder<Order>()
				.setEncoding("windows-1250")
				.setDataObjects(orders)
				
				.addHeaderAndField("from_location", o -> location)
				.addHeaderAndField("destination_location", o -> "1")
				.addHeaderAndField("customer_phone_number", o -> o.getShippingInfo().getPhoneNumber())
				.addHeaderAndField("customer", o -> o.getContact())
				.addHeaderAndField("customer_full_name", o -> o.getShippingInfo().getFullName())
				.build();

		
		String base64Value = Base64.encodeBase64String(bytes);
		
		return new FileData("adresnice.csv", base64Value);
	}
	
	
	
	
	
}
