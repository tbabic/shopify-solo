package org.bytepoet.shopifysolo.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.models.RefundInvoice;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.services.PdfInvoiceService;
import org.bytepoet.shopifysolo.services.PdfRefundService;
import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path= {"/dummy-invoice"})
@RestController
public class DummyInvoiceController {
	
	@Autowired
	private PdfInvoiceService invoiceService;
	
	@Autowired
	private PdfRefundService pdfRefundService;
	
	@Autowired
	private ShopifyApiClient shopifyApiClient;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Value("${soloapi.refund-note}")
	private String refundNote;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;

	@RequestMapping(method = RequestMethod.POST)
	public void dummy(@RequestBody PaymentOrder order) throws Exception {
		byte[] bytes = invoiceService.createInvoice(order, false, null);
		File file = new File("D:\\"+order.getInvoiceNumber()+".pdf");
		OutputStream os = new FileOutputStream(file);
		os.write(bytes);
		os.close();
	}
	
	@RequestMapping(path= {"/refund"}, method = RequestMethod.POST)
	public void dummyRefound(@RequestBody PaymentOrder order, @RequestParam("invoice") String invoiceNumber) throws Exception {
		List<Long> itemIds = order.getItems().stream().map(i -> i.getId()).collect(Collectors.toList());
		Refund refund = order.createRefund(itemIds);
		
		RefundInvoice invoice = new RefundInvoice.Builder()
				.date(order.getPaymentDate())
				.id(order.getInvoiceId())
				.jir(order.getInvoice().getJir())
				.zki(order.getInvoice().getZki())
				.note(remark(invoiceNumber))
				.number(order.getInvoiceNumber())
				.build();
		refund.setInvoice(invoice);
		
		byte [] bytes = pdfRefundService.createInvoice(refund);
		File file = new File("D:\\"+order.getInvoiceNumber()+".pdf");
		OutputStream os = new FileOutputStream(file);
		os.write(bytes);
		os.close();
	}
	
	private String remark(String invoiceNumber) {
		String note = MessageFormat.format(refundNote, invoiceNumber);
		return note;
	}
	
	
	@RequestMapping(method = RequestMethod.GET) 
	public void syncPayments() throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 19);
		cal.set(Calendar.MONTH, 8);
		List<PaymentOrder> orders = orderRepository.getByPamentDateBetween(cal.getTime(), new Date());
		List<PaymentOrder> transactionOrders = orders.stream()
				.filter(o -> o.getPaymentType() == PaymentType.BANK_TRANSACTION)
				.collect(Collectors.toList());
		
		for (PaymentOrder paymentOrder : transactionOrders) {
			
			if (!paymentOrder.isPaid()) {
				continue;
			}
			if (paymentOrder.getPaymentType() != PaymentType.BANK_TRANSACTION) {
				continue;
			}
			
			List<ShopifyTransaction> transactions  = shopifyApiClient.getTransactions(paymentOrder.getShopifyOrderId());
			if (transactions.size() != 1) {
				continue;
			}
			ShopifyTransaction transaction = transactions.get(0);
			if (!"pending".equalsIgnoreCase(transaction.getStatus())) {
				continue;
			}
			
			shopifyApiClient.createTransaction(transaction.createNewTransaction(), paymentOrder.getShopifyOrderId());
			
		}
		
		
	}
}

