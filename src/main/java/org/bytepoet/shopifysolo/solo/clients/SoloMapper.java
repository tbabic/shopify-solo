package org.bytepoet.shopifysolo.solo.clients;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.bytepoet.shopifysolo.solo.models.SoloBillingObject;
import org.bytepoet.shopifysolo.solo.models.SoloInvoice;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
class SoloMapper {

	MultiValueMap<String, String> map(SoloInvoice receipt) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		addToMap(map, receipt);
		if (receipt.getReceiptType() != null) {
			map.add("tip_racuna", receipt.getReceiptType());
		}
		map.add("fiskalizacija", receipt.isFiscal() ? "1" : "0");
		return map;
	}
	
	MultiValueMap<String, String> map(SoloTender tender) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		addToMap(map, tender);
		if (StringUtils.isNotBlank(tender.getNumber())) {
			map.add("broj_ponude", tender.getNumber());
		}
		return map;
	}

	private void addToMap(MultiValueMap<String, String> map, SoloBillingObject billing) {
		if (billing.getServiceType() != null) {
			map.add("tip_usluge", billing.getServiceType());
		}
		if (billing.getPaymentType() != null) {
			map.add("nacin_placanja", String.valueOf(billing.getPaymentType().getValue()));
		}
		if (billing.getEmail() != null) {
			map.add("kupac_naziv", billing.getEmail());
		}
		if (StringUtils.isNotBlank(billing.getNote())) {
			map.add("napomene", billing.getNote());
		}	
		map.add("prikazi_porez", billing.isTaxed() ? "1" : "0");
		
		addToMap(map, billing.getProducts());
	}
	
	private void addToMap(MultiValueMap<String, String> map, List<SoloProduct> products) {
		int i = 0;
		for (SoloProduct product : products) {
			i++; // start with one
			addToMap(map, product, i);
		}
	}
	
	private void addToMap(MultiValueMap<String, String> map, SoloProduct product, int ordinal) {
		String n = Integer.toString(ordinal);
		map.add("usluga", n);
		if (product.getName() != null) {
			map.add("opis_usluge_"+n, product.getName());
		}
		if (product.getUnit() != null) {
			map.add("jed_mjera_"+n, product.getUnit());
		}
		if (product.getPrice() != null) {
			map.add("cijena_"+n, convertDecimal(product.getPrice()));
		}
		map.add("kolicina_"+n, Integer.toString(product.getQuantity()));
		if (product.getDiscount() != null) {
			map.add("popust_"+n, convertDecimal(product.getDiscount()));
		}
		if (product.getTaxRate() != null) {
			map.add("porez_stopa_"+n, product.getTaxRate());
		}
		
	}
	
	
	private String convertDecimal(String decimalPointPrice) {
		return decimalPointPrice.replaceFirst("\\.", ",");
	}
	
}
