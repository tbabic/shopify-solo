package org.bytepoet.shopifysolo.controllers;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.DiscountService;
import org.bytepoet.shopifysolo.services.InventoryUpdateService;
import org.bytepoet.shopifysolo.services.InvoiceService;
import org.bytepoet.shopifysolo.services.MailService;
import org.bytepoet.shopifysolo.services.PdfInvoiceService;
import org.bytepoet.shopifysolo.services.MailService.MailAttachment;
import org.bytepoet.shopifysolo.services.MailService.MailReceipient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.ContentCachingRequestWrapper;

@RequestMapping("/orders")
@RestController
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private AuthorizationService authorizationService;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private GatewayToPaymentTypeMapper paymentTypeMapper;
	
	@Autowired
	private PdfInvoiceService pdfInvoiceService;
	
	@Value("${email.subject}")
	private String subject;
	
	@Value("${email.body}")
	private String body;
	
	@Value("${email.priority-body}")
	private String priorityBody;
	
	@Value("${ignore.receipt}")
	private String ignoreReceipts;
	
	@Value("${email.always-bcc:}")
	private String alwaysBcc;
	
	@Value("${solofy.tax-rate:}")
	private String taxRate;
	
	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Value("${shopify.gift-code-type}")
	private String giftCodeType;
	
	@Value("${soloapi.gift-code-note}")
	private String giftCodeNote;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Autowired
	private DiscountService discountService;
	
	@Autowired
	private InventoryUpdateService inventoryUpdateService;
	
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder shopifyOrder, ContentCachingRequestWrapper request) throws Exception {
		authorizationService.processRequest(request);
		if(shopifyOrder.getTags().contains("giveaway")) {
			return;
		}
		List<String> ignoreReceiptsList = Arrays.asList(ignoreReceipts.split(","));
		if (ignoreReceiptsList.contains(shopifyOrder.getNumber())) {
			return;
		}
		logger.debug(shopifyOrder.toString());
		PaymentOrder order;
		synchronized(this.getClass()) {
			Order o1 = orderRepository.getOrderWithShopifyId(shopifyOrder.getId()).orElseGet(() -> {
				return orderRepository.saveAndFlush(new PaymentOrder(shopifyOrder, paymentTypeMapper, taxRate, giftCodeType));
			});
			if (o1 instanceof PaymentOrder) {
				order = (PaymentOrder) o1;
			} else {
				return;
			}
		

			if (!order.isReceiptCreated()) {
				Invoice createdInvoice = CachedFunctionalService.<ShopifyOrder,Invoice>cacheAndExecute(
						shopifyOrder, 
						o -> "orders/"+o.getId(), 
						o -> {
							return invoiceService.createInvoice(order);
						});
				order.updateInvoice(createdInvoice);
				orderRepository.saveAndFlush(order);
			}
			
			if(!order.isReceiptSent()) {
				byte [] pdfInvoice = pdfInvoiceService.createInvoice(order, false, null);
				sendEmail(order.getEmail(), order.getInvoiceNumber(), pdfInvoice, order.isPriorityShipping());
				order.setReceiptSent(true);
				orderRepository.save(order);
				//TODO: upload pdf Invoice to google drive
			}
			
			if(!CollectionUtils.isEmpty(shopifyOrder.getDiscountCodes()) && shopifyOrder.getDiscountCodes().get(0).getType().equalsIgnoreCase("fixed_amount")) {
				String discountCode = shopifyOrder.getDiscountCodes().get(0).getCode();
				boolean hasDiscount = CachedFunctionalService.<ShopifyOrder, Boolean>cacheAndExecute(
						shopifyOrder,
						o -> "discounts/" + o.getId() + "/" + discountCode,
						o -> {
							return discountService.processDiscount(shopifyOrder.getDiscountCodes().get(0));
						});
				if(hasDiscount) {
					order.addNote("\n" + MessageFormat.format(giftCodeNote, discountCode));
					orderRepository.saveAndFlush(order);
				}
			}
			
			inventoryUpdateService.updateInventory(order, shopifyOrder);
		}
		return;
	}
	
	private void sendEmail(String email, String invoiceNumber, byte[] pdfInvoice, boolean isPriority) throws Exception {
		
		MailReceipient to = new MailReceipient(email);
		if (StringUtils.isNotBlank(alwaysBcc)) {
			to.bcc(alwaysBcc);
		}
		
		MailAttachment attachment = new MailAttachment()
				.filename(invoiceNumber + ".pdf")
				.mimeType("application/pdf")
				.content(new ByteArrayInputStream(pdfInvoice));	
		
		String emailBody = isPriority ? this.priorityBody : this.body;
		
		mailService.sendEmail(to, subject, emailBody, Collections.singletonList(attachment));
	}
	
}
