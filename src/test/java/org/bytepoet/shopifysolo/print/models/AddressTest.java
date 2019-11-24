package org.bytepoet.shopifysolo.print.models;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AddressTest {

	@Test
	public void constructor_SimpleAddress_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123\r\n" + 
				"12345 City Name";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
	}
	
	@Test
	public void constructor_SimpleAddressWithNumberCity_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123\r\n" + 
				"12345 City Name 78";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name 78"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
		
	}
	
	@Test
	public void constructor_SimpleAddressWithCharInStreetNumber_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123a\r\n" + 
				"12345 City Name 78";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123a"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name 78"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
		
	}
	
	@Test
	public void constructor_SimpleAddressWithCharAndWhitespaceInStreetNumber_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123 a\r\n" + 
				"12345 City Name 78";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123 a"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name 78"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
		
	}
	
	
	@Test
	public void constructor_AddressWithCompany_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"Company name ltd.\r\n" + 
				"This is Street 123\r\n" + 
				"12345 City Name";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), equalTo("Company name ltd."));
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
	}
	
	
	@Test
	public void constructor_AddressWithOther_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123\r\n" +
				"Other info.\r\n" + 
				"12345 City Name";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), equalTo("Other info."));
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
	}
	
	@Test
	public void constructor_AddressWithCountry_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123\r\n" +
				"12345 City Name\r\n" + 
				"Some country";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), equalTo("Some country"));
	}
	
	@Test
	public void constructor_AddressWithCompanyAndOther_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"Company name ltd.\r\n" + 
				"This is Street 123\r\n" +
				"Other info.\r\n" +
				"12345 City Name";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), equalTo("Company name ltd."));
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), equalTo("Other info."));
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), nullValue());
	}
	
	@Test
	public void constructor_AddressWithCompanyAndCountry_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"Company name ltd.\r\n" + 
				"This is Street 123\r\n" +
				"12345 City Name\r\n" + 
				"Some country";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), equalTo("Company name ltd."));
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), nullValue());
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), equalTo("Some country"));
	}
	
	@Test
	public void constructor_AddressWithOtherAndCountry_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"This is Street 123\r\n" +
				"Other info.\r\n" +
				"12345 City Name\r\n" + 
				"Some country";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), nullValue());
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), equalTo("Other info."));
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), equalTo("Some country"));
	}
	
	@Test
	public void constructor_AddressWithCompanyOtherAndCountry_ParsedOk() {
		String addressToParse = 
				"FirstName LastName\r\n" + 
				"Company name ltd.\r\n" + 
				"This is Street 123\r\n" +
				"Other info.\r\n" +
				"12345 City Name\r\n" + 
				"Some country";
		
		Address address = new Address(addressToParse);
		Assert.assertThat(address.getFullName(), equalTo("FirstName LastName"));
		Assert.assertThat(address.getCompanyName(), equalTo("Company name ltd."));
		Assert.assertThat(address.getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(address.getOther(), equalTo("Other info."));
		Assert.assertThat(address.getCity(), equalTo("City Name"));
		Assert.assertThat(address.getPostalCode(), equalTo("12345"));
		Assert.assertThat(address.getCountry(), equalTo("Some country"));
	}
	
	
	@Test
	public void parseMultipleAddress_EmptyAddressString_ParsedOk() {
		String adressesToParse = "";
		List<Address> adressList = Address.parseMultipleAddress(adressesToParse);
		Assert.assertThat(adressList.size(), equalTo(0));
	}
	
	@Test
	public void parseMultipleAddress_SingleAddressString_ParsedOk() {
		String adressesToParse = 
				"\"FirstName1 LastName1\r\n" + 
				"This is Street 123\r\n" + 
				"12345 City Name1\"";

		
		List<Address> adressList = Address.parseMultipleAddress(adressesToParse);
		Assert.assertThat(adressList.size(), equalTo(1));
		
		Assert.assertThat(adressList.get(0).getFullName(), equalTo("FirstName1 LastName1"));
		Assert.assertThat(adressList.get(0).getCompanyName(), nullValue());
		Assert.assertThat(adressList.get(0).getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(adressList.get(0).getOther(), nullValue());
		Assert.assertThat(adressList.get(0).getCity(), equalTo("City Name1"));
		Assert.assertThat(adressList.get(0).getPostalCode(), equalTo("12345"));
		Assert.assertThat(adressList.get(0).getCountry(), nullValue());
	}
	
	@Test
	public void parseMultipleAddress_MultipleAddressString_ParsedOk() {
		String adressesToParse = 
				"\"FirstName1 LastName1\r\n" + 
				"This is Street 123\r\n" + 
				"12345 City Name1\"\r\n" + 
				"\r\n" + 
				"\"FirstName2 LastName2\r\n" + 
				"This is Street 124\r\n" + 
				"12346 City Name2\"\r\n" + 
				"\"FirstName3 LastName3\r\n" + 
				"This is Street 125\r\n" + 
				"12347 City Name3\"";
		
		List<Address> adressList = Address.parseMultipleAddress(adressesToParse);
		Assert.assertThat(adressList.size(), equalTo(3));
		
		Assert.assertThat(adressList.get(0).getFullName(), equalTo("FirstName1 LastName1"));
		Assert.assertThat(adressList.get(0).getCompanyName(), nullValue());
		Assert.assertThat(adressList.get(0).getAddress(), equalTo("This is Street 123"));
		Assert.assertThat(adressList.get(0).getOther(), nullValue());
		Assert.assertThat(adressList.get(0).getCity(), equalTo("City Name1"));
		Assert.assertThat(adressList.get(0).getPostalCode(), equalTo("12345"));
		Assert.assertThat(adressList.get(0).getCountry(), nullValue());
		
		Assert.assertThat(adressList.get(1).getFullName(), equalTo("FirstName2 LastName2"));
		Assert.assertThat(adressList.get(1).getCompanyName(), nullValue());
		Assert.assertThat(adressList.get(1).getAddress(), equalTo("This is Street 124"));
		Assert.assertThat(adressList.get(1).getOther(), nullValue());
		Assert.assertThat(adressList.get(1).getCity(), equalTo("City Name2"));
		Assert.assertThat(adressList.get(1).getPostalCode(), equalTo("12346"));
		Assert.assertThat(adressList.get(1).getCountry(), nullValue());
		
		Assert.assertThat(adressList.get(2).getFullName(), equalTo("FirstName3 LastName3"));
		Assert.assertThat(adressList.get(2).getCompanyName(), nullValue());
		Assert.assertThat(adressList.get(2).getAddress(), equalTo("This is Street 125"));
		Assert.assertThat(adressList.get(2).getOther(), nullValue());
		Assert.assertThat(adressList.get(2).getCity(), equalTo("City Name3"));
		Assert.assertThat(adressList.get(2).getPostalCode(), equalTo("12347"));
		Assert.assertThat(adressList.get(2).getCountry(), nullValue());
	}
	
}
