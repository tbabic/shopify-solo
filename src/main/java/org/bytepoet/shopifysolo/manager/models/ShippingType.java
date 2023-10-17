package org.bytepoet.shopifysolo.manager.models;

public enum ShippingType {

		HP_REGISTERED_MAIL,
		GLS_DELIVERY,
		PERSONAL_TAKEOVER;
		
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
