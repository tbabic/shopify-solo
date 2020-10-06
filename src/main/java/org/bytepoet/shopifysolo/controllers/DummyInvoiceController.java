package org.bytepoet.shopifysolo.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.services.PdfInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path= {"/dummy-invoice"})
@RestController
public class DummyInvoiceController {
	
	@Autowired
	private PdfInvoiceService invoiceService;

	@RequestMapping(method = RequestMethod.POST)
	public void dummy(@RequestBody PaymentOrder order) throws Exception {
		byte[] bytes = invoiceService.createInvoice(order, false, null);
		File file = new File("D:\\dummy-invoice.pdf");
		OutputStream os = new FileOutputStream(file);
		os.write(bytes);
		os.close();
	}
}

