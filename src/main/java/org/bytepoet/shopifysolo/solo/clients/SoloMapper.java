package org.bytepoet.shopifysolo.solo.clients;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
class SoloMapper {

	MultiValueMap<String, String> map(SoloReceipt receipt) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		addToMap(map, receipt);
		return map;
	}

	private void addToMap(MultiValueMap<String, String> map, SoloReceipt receipt) {
		if (receipt.getReceiptType() != null) {
			map.add("tip_racuna", receipt.getReceiptType());
		}
		if (receipt.getServiceType() != null) {
			map.add("tip_usluge", receipt.getServiceType());
		}
		if (receipt.getPaymentType() != null) {
			map.add("nacin_placanja", receipt.getPaymentType());
		}
		if (receipt.getEmail() != null) {
			map.add("kupac_naziv", receipt.getEmail());
		}
		if (StringUtils.isNotBlank(receipt.getNote())) {
			map.add("napomene", receipt.getNote());
		}	
		map.add("prikazi_porez", receipt.isTaxed() ? "1" : "0");
		map.add("fiskalizacija", receipt.isFiscal() ? "1" : "0");
		addToMap(map, receipt.getProducts());
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
