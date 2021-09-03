package org.bytepoet.shopifysolo.services;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.bytepoet.shopifysolo.shopify.clients.ShopifyApiClient;
import org.bytepoet.shopifysolo.shopify.models.ShopifyDiscountCode;
import org.bytepoet.shopifysolo.shopify.models.ShopifyPriceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountService {

	@Autowired
	private ShopifyApiClient client;
	
	
	public boolean processDiscount(ShopifyDiscountCode shopifyDiscountCode) {
		String discountCode = shopifyDiscountCode.getCode();
		if (!discountCode.startsWith("050") 
				&& !discountCode.startsWith("150")
				&& !discountCode.startsWith("200")
				&& !discountCode.startsWith("250")
				&& !discountCode.startsWith("400")) {
			return false;
		}
		
		try {
			ShopifyPriceRule priceRule = client.getDiscountPriceRule(discountCode);
			
			if (priceRule.getPriceRule().getValueType().equals("fixed_amount")
					&& priceRule.getPriceRule().getTitle().length() == 10
					&& priceRule.getPriceRule().getTitle().equals(discountCode)) {
				
				priceRule.getPriceRule().increaseUsageLimit(1);
				
				double discountAmount = Double.parseDouble(shopifyDiscountCode.getAmount());
				double totalAmount = Double.parseDouble(priceRule.getPriceRule().getValue());
				totalAmount += discountAmount;
				if (totalAmount >= 0) {
					return true;
				}

				DecimalFormat df = getDecimalFormat();
				priceRule.getPriceRule().setValue(df.format(totalAmount));
				client.updatePriceRule(priceRule);
			}
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return true;
		
	}
	
	private DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(newSymbols);
		return df;
	}
}
