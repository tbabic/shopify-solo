package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.CsvBuilder;
import org.bytepoet.shopifysolo.manager.models.FileData;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.springframework.stereotype.Service;

@Service
public class GlsService {

	
	public FileData generateAddressBookCsv(List<Order> orders) {
		
		validate(orders);
		
		byte [] bytes = new CsvBuilder<Order>()
				.setEncoding("windows-1250")
				.setDataObjects(orders)
				
				.addHeaderAndField("Ime i prezime", o -> o.getShippingInfo().getFullName())
				.addHeaderAndField("Država", o -> IsoCountriesService.getCountryCode(o.getShippingInfo().getCountry()))
				.addHeaderAndField("Poštanski broj", o -> o.getShippingInfo().getPostalCode())
				.addHeaderAndField("Grad", o -> o.getShippingInfo().getCity())
				.addHeaderAndField("Ulica", o -> o.getShippingInfo().getStreetAndNumber())
				.addHeaderAndField("Kućni broj", o -> "")
				.addHeaderAndField("Stubište", o -> "")
				.addHeaderAndField("Kontakt osoba", o -> o.getShippingInfo().getFullName())
				.addHeaderAndField("Kontakt telefon", o -> o.getShippingInfo().getPhoneNumber())
				.addHeaderAndField("Kontakt email", o -> o.getContact())
				.addHeaderAndField("Komentar", o -> StringUtils.defaultString(o.getShippingInfo().getOther()))
				.addHeaderAndField("Količina paketa", o -> "1")
				.addHeaderAndField("Referenca klijenta", o -> o.getId().toString())
				.addHeaderAndField("Reference pouzeća", o -> "")
				.addHeaderAndField("Iznos pouzeća", o -> "0")
				.addHeaderAndField("Usluge", o -> "")
				.build();

		
		String base64Value = Base64.encodeBase64String(bytes);
		
		return new FileData("adresnice.csv", base64Value);
	}
	
	public void validate(List<Order> orders) {
		boolean invalid = false;
		String message = "INVALID:";
		String regex = ".*[ ][0-9/]+[ ]?[a-zA-Z]?";
		
		for (Order order: orders) {
			
			String address = order.getShippingInfo().getStreetAndNumber();
			if (!address.matches(regex)) {
				invalid = true;
				message += "\n " + order.getId() + " - " + order.getShippingInfo().getFullName() + ";";
			}
		}
		
		if (invalid) {
			throw new RuntimeException(message);
		}
	}
	
	
	
}
