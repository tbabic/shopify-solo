package org.bytepoet.shopifysolo.controllers;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.bytepoet.shopifysolo.authorization.AuthorizationService;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.mappers.GatewayToPaymentTypeMapper;
import org.bytepoet.shopifysolo.services.CachedFunctionalService;
import org.bytepoet.shopifysolo.services.DiscountService;
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

@RequestMapping({"/tenders", "/drafts"})
@RestController
public class TenderController {
	
	private static final Logger logger = LoggerFactory.getLogger(TenderController.class);
	
	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private GatewayToPaymentTypeMapper paymentTypeMapper;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Value("${shopify.bank-deposit-gateway}")
	private List<String> bankDepositGateway;
	
	@Value("${email.tender-body}")
	private String body;
	
	@Value("${email.tender-subject}")
	private String subject;
	
	@Value("${email.tender-bcc:}")
	private String tenderBcc;
	
	@Value("${ignore.tender}")
	private String ignoreTenders;
	
	@Value("${solofy.tax-rate:}")
	private String taxRate;
	
	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Value("${shopify.gift-code-type}")
	private String giftCodeType;
	
	@Value("${soloapi.gift-code-note}")
	private String giftCodeNote;
	
	@Autowired
	private DiscountService discountService;
	
	@PostMapping
	public void postOrder(@RequestBody ShopifyOrder shopifyOrder, ContentCachingRequestWrapper request) throws Exception {
		authorizationService.processRequest(request);
		if (!bankDepositGateway.contains(shopifyOrder.getGateway())) {
			return;
		}
		List<String> ignoreReceiptsList = Arrays.asList(ignoreTenders.split(","));
		if (ignoreReceiptsList.contains(shopifyOrder.getNumber())) {
			return;
		}
		logger.debug(shopifyOrder.toString());
		PaymentOrder order;
		synchronized(this.getClass()) {
			order = orderRepository.getOrderWithShopifyId(shopifyOrder.getId()).orElseGet(() -> {
				return orderRepository.saveAndFlush(new PaymentOrder(shopifyOrder, paymentTypeMapper, taxRate, shippingTitle, giftCodeType));
			});
		}
		
		if(CollectionUtils.isEmpty(shopifyOrder.getDiscountCodes())) {
			return;
		}
		if(shopifyOrder.getDiscountCodes().get(0).getType().equalsIgnoreCase("fixed_amount")) {
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
	}

}
