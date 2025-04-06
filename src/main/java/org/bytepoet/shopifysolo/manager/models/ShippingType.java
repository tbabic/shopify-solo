package org.bytepoet.shopifysolo.manager.models;

public enum ShippingType {

		HP_REGISTERED_MAIL,
		GLS_DELIVERY,
		BOX_NOW,
		PERSONAL_TAKEOVER;
		
		public static ShippingType valueOfShippingTittle (String shopifyShippingTitle) {
			if (shopifyShippingTitle.toUpperCase().contains("HP")) {
				return HP_REGISTERED_MAIL;
			}
			if (shopifyShippingTitle.toUpperCase().contains("GLS")) {
				return GLS_DELIVERY;
			}
			if (shopifyShippingTitle.toUpperCase().contains("BOX")) {
				return BOX_NOW;
			}
			return HP_REGISTERED_MAIL;
			
		}
}
