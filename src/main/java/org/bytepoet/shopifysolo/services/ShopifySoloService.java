package org.bytepoet.shopifysolo.services;

import org.bytepoet.shopifysolo.mappers.ShopifyToSoloInvoiceMapper;
import org.bytepoet.shopifysolo.mappers.ShopifyToSoloTenderMapper;
import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.clients.SoloApiClient;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ShopifySoloService {

	@Autowired
	private SoloApiClient soloApiClient;
	
	@Autowired
	private ShopifyToSoloInvoiceMapper invoiceMapper;
	
	@Autowired
	private ShopifyToSoloTenderMapper tenderMapper;
	
	@Cacheable(value="invoice", key="#p0.id")
	public SoloInvoice createInvoice(ShopifyOrder shopifyOrder) {
		SoloInvoice invoice = invoiceMapper.map(shopifyOrder);
		return soloApiClient.createInvoice(invoice);
	}
	
	@Cacheable(value="tender", key="#p0.id")
	public SoloTender createTender(ShopifyOrder shopifyOrder) {
		SoloTender tender = tenderMapper.map(shopifyOrder);
		return soloApiClient.createTender(tender);
	}
	
	
}
