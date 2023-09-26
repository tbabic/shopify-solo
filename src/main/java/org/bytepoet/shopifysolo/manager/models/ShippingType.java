package org.bytepoet.shopifysolo.manager.models;

import org.bytepoet.shopifysolo.shopify.models.ShopifyShippingLine;

public enum ShippingType {

		HP_REGISTERED_MAIL,
		GLS_DELIVERY;
		
		public static ShippingType valueOf (ShopifyShippingLine shopifyShippingLine) {
			if (shopifyShippingLine.getTitle().toUpperCase().contains("HP")) {
				return HP_REGISTERED_MAIL;
			}
			return GLS_DELIVERY;
		}
}
