package org.bytepoet.shopifysolo.controllers;

import java.util.List;

import org.bytepoet.shopifysolo.print.models.Address;
import org.bytepoet.shopifysolo.print.models.Base64Wrapper;
import org.bytepoet.shopifysolo.print.models.MultipleAddressString;
import org.bytepoet.shopifysolo.services.PrintAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/adresses")
@RestController
public class PrintAdressController {

	@Autowired
	private PrintAddressService printAddressService;
	
	@PostMapping("/parse") 
	public List<Address> parseAddress(@RequestBody MultipleAddressString multipleAddressString) {
		return Address.parseMultipleAddress(multipleAddressString.getValue());
	}
	
	@PostMapping("/print") 
	public Base64Wrapper parseAddress(@RequestBody List<Address> addressList) throws Exception {
		return printAddressService.printToPostalFormPdf(addressList);
	}
	
	@PostMapping("/postal-form") 
	public Base64Wrapper printAddresses(@RequestBody List<org.bytepoet.shopifysolo.manager.models.Address> addressList) throws Exception {
		return printAddressService.printToPostalFormPdf(addressList);
	}
}
