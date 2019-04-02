package org.bytepoet.shopifysolo.solo.clients;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bytepoet.shopifysolo.solo.models.SoloProduct;
import org.bytepoet.shopifysolo.solo.models.SoloReceipt;
import org.springframework.stereotype.Service;

@Service
class SoloMapper {

	Map<String, String> map(SoloReceipt receipt) {
		Map<String, String> map = new LinkedHashMap<>();
		addToMap(map, receipt);
		return map;
	}

	private void addToMap(Map<String, String> map, SoloReceipt receipt) {
		if (receipt.getReceiptType() != null) {
			map.put("tip_racuna", receipt.getReceiptType());
		}
		if (receipt.getServiceType() != null) {
			map.put("tip_usluge", receipt.getServiceType());
		}
		if (receipt.getPaymentType() != null) {
			map.put("nacin_placanja", receipt.getPaymentType());
		}
		if (receipt.getEmail() != null) {
			map.put("kupac_naziv", receipt.getEmail());
		}		
		map.put("prikazi_porez", receipt.isTaxed() ? "1" : "0");
		if (receipt.getServiceType() != null) {
			map.put("tip_usluge", receipt.getServiceType());
		}
		
		addToMap(map, receipt.getProducts());
	}
	
	private Map<String, String> addToMap(Map<String, String> map, List<SoloProduct> products) {
		int i = 0;
		for (SoloProduct product : products) {
			i++; // start with one
			addToMap(map, product, i);
		}
		return map;
	}
	
	private void addToMap(Map<String, String> map, SoloProduct product, int ordinal) {
		String n = Integer.toString(ordinal);
		map.put("usluga", n);
		if (product.getName() != null) {
			map.put("opis_usluge_"+n, product.getName());
		}
		if (product.getUnit() != null) {
			map.put("jed_mjera_"+n, product.getUnit());
		}
		if (product.getPrice() != null) {
			map.put("cijena_"+n, convertPrice(product.getPrice()));
		}
		map.put("kolicina_"+n, Integer.toString(product.getQuantity()));
		if (product.getDiscount() != null) {
			map.put("popust_"+n, product.getDiscount());
		}
		if (product.getTaxRate() != null) {
			map.put("porez_stopa_"+n, product.getTaxRate());
		}
		
	}
	
	
	private String convertPrice(String decimalPointPrice) {
		return decimalPointPrice.replaceFirst("\\.", ",");
	}
	
}
