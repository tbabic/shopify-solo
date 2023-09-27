package org.bytepoet.shopifysolo.manager.models;

import org.bytepoet.shopifysolo.shopify.models.ShopifyShippingLine;

public enum ShippingType {

		HP_REGISTERED_MAIL,
		GLS_DELIVERY;
		
		public static ShippingType valueOfShippingTittle (String shopifyShippingTitle) {
			if (shopifyShippingTitle.toUpperCase().contains("HP")) {
				return HP_REGISTERED_MAIL;
			}
			if (shopifyShippingTitle.toUpperCase().contains("GLS")) {
				return GLS_DELIVERY;
			}
			return HP_REGISTERED_MAIL;
			
		}
}
