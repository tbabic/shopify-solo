package org.bytepoet.shopifysolo.print.models;

import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

	@JsonProperty
	private String fullName;
	@JsonProperty
	private String companyName;
	@JsonProperty
	private String address;
	@JsonProperty
	private String other;
	@JsonProperty
	private String city;
	@JsonProperty
	private String postalCode;
	@JsonProperty
	private String country;
	
	@JsonProperty
	private String fullRecepient;
	@JsonProperty
	private String fullAddress;
	@JsonProperty
	private String fullDestination;
	
	@JsonProperty
	private String addressAndCity;
	
	protected Address() {}
	
	
	public Address(String fullAddressString) {
		String rowDelimiter = rowDelimiter(fullAddressString);
		String[] rows = fullAddressString.split(rowDelimiter);
		//fullName always at top
		this.fullName = rows[0];
		
		// country at bottom if it exists
		int countryOffset = 1;
		if (checkIsCountry(rows[rows.length-1])) {
			this.country = rows[rows.length-1];
			countryOffset = 2;
		}
		
		//postalCode and city at bottom or above country
		this.postalCode = extractPostalCode(rows[rows.length-countryOffset]);
		this.city = extractCity(rows[rows.length-countryOffset]);
		
		// company always second if it exists
		int companyOffset = 0;
		if (!checkIsAddress(rows[1])) {
			companyOffset = 1;
			this.companyName = rows[1];
		}
		
		// address second or below company
		int addressOffset = companyOffset + 1;
		this.address = rows[addressOffset];
		
		//other info below address and above postal code and city if it exists
		if (addressOffset +1 < rows.length - countryOffset) {
			this.other = rows[addressOffset+1];
		}
	}

	public static List<Address> parseMultipleAddress(String multipleAddressString) {
		String processingString = multipleAddressString;
		List<Address> list = new ArrayList<>();
		while(processingString.indexOf("\"") >= 0) {
			
			//remove the opening quotes
			processingString = processingString.replaceFirst("\"", "");
			//extract until the ending quotes
			String addressString = processingString.substring(0, processingString.indexOf("\""));
			list.add(new Address(addressString.trim()));
			
			//remove matched address
			processingString = processingString.substring(processingString.indexOf("\"")).trim();
			//remove ending quotes and trim
			processingString = processingString.replaceFirst("\"", "").trim();
			
		}
		return list;
	}
	
	private static String rowDelimiter(String string) {
		String rowDelimiter = "\n";
		if (string.contains("\r\n")) {
			rowDelimiter = "\r\n";
		}
		return rowDelimiter;
	}
	
	private String extractPostalCode(String postalCodeAndCity) {
		Pattern pattern = Pattern.compile("[0-9 ]+");
		Matcher matcher = pattern.matcher(postalCodeAndCity);
		
		if (matcher.find()) {
			return matcher.group().replaceAll(" ", "");
		}
		throw new RuntimeException("String does not contain postal code: " + postalCodeAndCity);
	}
	
	private String extractCity(String postalCodeAndCity) {
		return postalCodeAndCity.replaceFirst("[0-9 ]+", "");
	}
	
	private boolean checkIsCountry(String row) {
		String replaced = row.replaceFirst("[0-9]", "");
		if (replaced.equals(row)) {
			return true;
		}
		return false;
	}
	
	private boolean checkIsAddress(String row) {
		String replaced = row.replaceFirst("[0-9]+[a-zA-Z]?$", "");
		if (!replaced.equals(row)) {
			return true;
		}
		replaced = row.replaceFirst("[0-9]+ [a-zA-Z]?$", "");
		if (!replaced.equals(row)) {
			return true;
		}
		return false;
	}
	
	public String getFullRecepient() {
		if (StringUtils.isNotBlank(fullRecepient)) {
			return fullRecepient;
		}
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(companyName)) {
			sb.append(companyName);
		}
		if (StringUtils.isNoneBlank(companyName, fullName)) {
			sb.append(" n/r ");
		}
		if (StringUtils.isNotBlank(fullName)) {
			sb.append(fullName);
		}
		fullRecepient = sb.toString();
		return fullRecepient;
	}
	
	public String getFullAddress() {
		if (StringUtils.isNotBlank(fullAddress)) {
			return fullAddress;
		}
		StringBuilder sb = new StringBuilder(address);
		if (StringUtils.isNotBlank(other)) {
			sb.append(", ");
			sb.append(other);
		}
		fullAddress = sb.toString();
		return fullAddress;
	}
	
	public String getFullDestination() {
		if (StringUtils.isNotBlank(fullDestination)) {
			return fullDestination;
		}
		StringBuilder sb = new StringBuilder(postalCode);
		sb.append(", ");
		sb.append(city);
		fullDestination = sb.toString();
		return fullDestination;
	}
	
	public String getAddressAndCity() {
		if (StringUtils.isNotBlank(addressAndCity)) {
			return addressAndCity;
		}
		StringBuilder sb = new StringBuilder(getFullAddress());
		sb.append(", ");
		sb.append(city);
		addressAndCity = sb.toString();
		return addressAndCity;
		
	}
	
	
	
	
	public String getFullName() {
		return fullName;
	}
	public String getCompanyName() {
		return companyName;
	}
	public String getAddress() {
		return address;
	}
	public String getOther() {
		return other;
	}
	public String getCity() {
		return city;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public String getCountry() {
		return country;
	}
	
	
	
	
	
	
	
}
