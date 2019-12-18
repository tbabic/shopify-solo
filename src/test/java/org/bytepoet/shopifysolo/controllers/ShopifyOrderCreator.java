package org.bytepoet.shopifysolo.controllers;

import java.io.IOException;
import java.text.MessageFormat;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShopifyOrderCreator {

	
	
	public static ShopifyOrder createOrder(String id) {
		String json = MessageFormat.format(SHOPIFY_CORVUSPAY_ORDER_FORMAT, id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return mapper.readValue(json, ShopifyOrder.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static ShopifyOrder createTender(String id) {
		String json = MessageFormat.format(SHOPIFY_BANK_TRANSACTION_ORDER_FORMAT, id);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {
			return mapper.readValue(json, ShopifyOrder.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	private static final String SHOPIFY_CORVUSPAY_ORDER_FORMAT = 
			"'{'\"id\":{0},\"email\":\"jon@doe.ca\",\"closed_at\":null,\"created_at\":\"2019-04-02T18:33:59+02:00\",\"updated_at\":\"2019-04-02T18:33:59+02:00\",\"number\":234,\"note\":null,\"token\":\"123456abcd\",\"gateway\":\"corvuspay\",\"test\":true,\"total_price\":\"463.00\",\"subtotal_price\":\"453.00\",\"total_weight\":0,\"total_tax\":\"0.00\",\"taxes_included\":false,\"currency\":\"HRK\",\"financial_status\":\"voided\",\"confirmed\":false,\"total_discounts\":\"5.00\",\"total_line_items_price\":\"458.00\",\"cart_token\":null,\"buyer_accepts_marketing\":true,\"name\":\"#9999\",\"referring_site\":null,\"landing_site\":null,\"cancelled_at\":\"2019-04-02T18:33:59+02:00\",\"cancel_reason\":\"customer\",\"total_price_usd\":null,\"checkout_token\":null,\"reference\":null,\"user_id\":null,\"location_id\":null,\"source_identifier\":null,\"source_url\":null,\"processed_at\":null,\"device_id\":null,\"phone\":null,\"customer_locale\":\"hr\",\"app_id\":null,\"browser_ip\":null,\"landing_site_ref\":null,\"order_number\":1234,\"discount_applications\":['{'\"type\":\"manual\",\"value\":\"5.0\",\"value_type\":\"fixed_amount\",\"allocation_method\":\"one\",\"target_selection\":\"explicit\",\"target_type\":\"line_item\",\"description\":\"Discount\",\"title\":\"Discount\"'}'],\"discount_codes\":[],\"note_attributes\":[],\"payment_gateway_names\":[\"visa\",\"bogus\"],\"processing_method\":\"\",\"checkout_id\":null,\"source_name\":\"web\",\"fulfillment_status\":\"pending\",\"tax_lines\":[],\"tags\":\"\",\"contact_email\":\"jon@doe.ca\",\"order_status_url\":\"https:\\/\\/checkout.shopify.com\\/9550921785\\/orders\\/123456abcd\\/authenticate?key=abcdefg\",\"presentment_currency\":\"HRK\",\"total_line_items_price_set\":'{'\"shop_money\":'{'\"amount\":\"458.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"458.00\",\"currency_code\":\"HRK\"'}}',\"total_discounts_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}',\"total_shipping_price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"subtotal_price_set\":'{'\"shop_money\":'{'\"amount\":\"453.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"453.00\",\"currency_code\":\"HRK\"'}}',\"total_price_set\":'{'\"shop_money\":'{'\"amount\":\"463.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"463.00\",\"currency_code\":\"HRK\"'}}',\"total_tax_set\":'{'\"shop_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}}',\"total_tip_received\":\"0.0\",\"line_items\":['{'\"id\":866550311766439020,\"variant_id\":null,\"title\":\"Nau�nice PERLLA\",\"quantity\":1,\"sku\":\"\",\"variant_title\":null,\"vendor\":null,\"fulfillment_service\":\"manual\",\"product_id\":2122896638009,\"requires_shipping\":true,\"taxable\":true,\"gift_card\":false,\"name\":\"Nau�nice PERLLA\",\"variant_inventory_management\":null,\"properties\":[],\"product_exists\":true,\"fulfillable_quantity\":1,\"grams\":0,\"price\":\"229.00\",\"total_discount\":\"0.00\",\"fulfillment_status\":null,\"price_set\":'{'\"shop_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}}',\"total_discount_set\":'{'\"shop_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":[],\"tax_lines\":[]'}','{'\"id\":141249953214522974,\"variant_id\":null,\"title\":\"Nau�nice PERLLA\",\"quantity\":1,\"sku\":\"\",\"variant_title\":null,\"vendor\":null,\"fulfillment_service\":\"manual\",\"product_id\":2122896638009,\"requires_shipping\":true,\"taxable\":true,\"gift_card\":false,\"name\":\"Nau�nice PERLLA\",\"variant_inventory_management\":null,\"properties\":[],\"product_exists\":true,\"fulfillable_quantity\":1,\"grams\":0,\"price\":\"229.00\",\"total_discount\":\"5.00\",\"fulfillment_status\":null,\"price_set\":'{'\"shop_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}}',\"total_discount_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":['{'\"amount\":\"5.00\",\"discount_application_index\":0,\"amount_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}}'],\"tax_lines\":[]'}'],\"shipping_lines\":['{'\"id\":271878346596884015,\"title\":\"Generic Shipping\",\"price\":\"10.00\",\"code\":null,\"source\":\"shopify\",\"phone\":null,\"requested_fulfillment_service_id\":null,\"delivery_category\":null,\"carrier_identifier\":null,\"discounted_price\":\"10.00\",\"price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"discounted_price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":[],\"tax_lines\":[]'}'],\"billing_address\":'{'\"first_name\":\"Bob\",\"address1\":\"123 Billing Street\",\"phone\":\"555-555-BILL\",\"city\":\"Billtown\",\"zip\":\"K2P0B0\",\"province\":\"Kentucky\",\"country\":\"United States\",\"last_name\":\"Biller\",\"address2\":null,\"company\":\"My Company\",\"latitude\":null,\"longitude\":null,\"name\":\"Bob Biller\",\"country_code\":\"US\",\"province_code\":\"KY\"'}',\"shipping_address\":'{'\"first_name\":\"Steve\",\"address1\":\"123 Shipping Street\",\"phone\":\"555-555-SHIP\",\"city\":\"Shippington\",\"zip\":\"40003\",\"province\":\"Kentucky\",\"country\":\"United States\",\"last_name\":\"Shipper\",\"address2\":null,\"company\":\"Shipping Company\",\"latitude\":null,\"longitude\":null,\"name\":\"Steve Shipper\",\"country_code\":\"US\",\"province_code\":\"KY\"'}',\"fulfillments\":[],\"refunds\":[],\"customer\":'{'\"id\":115310627314723954,\"email\":\"john@test.com\",\"accepts_marketing\":false,\"created_at\":null,\"updated_at\":null,\"first_name\":\"John\",\"last_name\":\"Smith\",\"orders_count\":0,\"state\":\"disabled\",\"total_spent\":\"0.00\",\"last_order_id\":null,\"note\":null,\"verified_email\":true,\"multipass_identifier\":null,\"tax_exempt\":false,\"phone\":null,\"tags\":\"\",\"last_order_name\":null,\"currency\":\"HRK\",\"accepts_marketing_updated_at\":null,\"marketing_opt_in_level\":null,\"default_address\":'{'\"id\":715243470612851245,\"customer_id\":115310627314723954,\"first_name\":null,\"last_name\":null,\"company\":null,\"address1\":\"123 Elm St.\",\"address2\":null,\"city\":\"Ottawa\",\"province\":\"Ontario\",\"country\":\"Canada\",\"zip\":\"K2H7A8\",\"phone\":\"123-123-1234\",\"name\":\"\",\"province_code\":\"ON\",\"country_code\":\"CA\",\"country_name\":\"Canada\",\"default\":true'}}}'";
	
	private static final String SHOPIFY_BANK_TRANSACTION_ORDER_FORMAT = 
			"'{'\"id\":{0},\"email\":\"jon@doe.ca\",\"closed_at\":null,\"created_at\":\"2019-04-02T18:33:59+02:00\",\"updated_at\":\"2019-04-02T18:33:59+02:00\",\"number\":234,\"note\":null,\"token\":\"123456abcd\",\"gateway\":\"Uplata na račun\",\"test\":true,\"total_price\":\"463.00\",\"subtotal_price\":\"453.00\",\"total_weight\":0,\"total_tax\":\"0.00\",\"taxes_included\":false,\"currency\":\"HRK\",\"financial_status\":\"voided\",\"confirmed\":false,\"total_discounts\":\"5.00\",\"total_line_items_price\":\"458.00\",\"cart_token\":null,\"buyer_accepts_marketing\":true,\"name\":\"#9999\",\"referring_site\":null,\"landing_site\":null,\"cancelled_at\":\"2019-04-02T18:33:59+02:00\",\"cancel_reason\":\"customer\",\"total_price_usd\":null,\"checkout_token\":null,\"reference\":null,\"user_id\":null,\"location_id\":null,\"source_identifier\":null,\"source_url\":null,\"processed_at\":null,\"device_id\":null,\"phone\":null,\"customer_locale\":\"hr\",\"app_id\":null,\"browser_ip\":null,\"landing_site_ref\":null,\"order_number\":1234,\"discount_applications\":['{'\"type\":\"manual\",\"value\":\"5.0\",\"value_type\":\"fixed_amount\",\"allocation_method\":\"one\",\"target_selection\":\"explicit\",\"target_type\":\"line_item\",\"description\":\"Discount\",\"title\":\"Discount\"'}'],\"discount_codes\":[],\"note_attributes\":[],\"payment_gateway_names\":[\"visa\",\"bogus\"],\"processing_method\":\"\",\"checkout_id\":null,\"source_name\":\"web\",\"fulfillment_status\":\"pending\",\"tax_lines\":[],\"tags\":\"\",\"contact_email\":\"jon@doe.ca\",\"order_status_url\":\"https:\\/\\/checkout.shopify.com\\/9550921785\\/orders\\/123456abcd\\/authenticate?key=abcdefg\",\"presentment_currency\":\"HRK\",\"total_line_items_price_set\":'{'\"shop_money\":'{'\"amount\":\"458.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"458.00\",\"currency_code\":\"HRK\"'}}',\"total_discounts_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}',\"total_shipping_price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"subtotal_price_set\":'{'\"shop_money\":'{'\"amount\":\"453.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"453.00\",\"currency_code\":\"HRK\"'}}',\"total_price_set\":'{'\"shop_money\":'{'\"amount\":\"463.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"463.00\",\"currency_code\":\"HRK\"'}}',\"total_tax_set\":'{'\"shop_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}}',\"total_tip_received\":\"0.0\",\"line_items\":['{'\"id\":866550311766439020,\"variant_id\":null,\"title\":\"Nau�nice PERLLA\",\"quantity\":1,\"sku\":\"\",\"variant_title\":null,\"vendor\":null,\"fulfillment_service\":\"manual\",\"product_id\":2122896638009,\"requires_shipping\":true,\"taxable\":true,\"gift_card\":false,\"name\":\"Nau�nice PERLLA\",\"variant_inventory_management\":null,\"properties\":[],\"product_exists\":true,\"fulfillable_quantity\":1,\"grams\":0,\"price\":\"229.00\",\"total_discount\":\"0.00\",\"fulfillment_status\":null,\"price_set\":'{'\"shop_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}}',\"total_discount_set\":'{'\"shop_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"0.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":[],\"tax_lines\":[]'}','{'\"id\":141249953214522974,\"variant_id\":null,\"title\":\"Nau�nice PERLLA\",\"quantity\":1,\"sku\":\"\",\"variant_title\":null,\"vendor\":null,\"fulfillment_service\":\"manual\",\"product_id\":2122896638009,\"requires_shipping\":true,\"taxable\":true,\"gift_card\":false,\"name\":\"Nau�nice PERLLA\",\"variant_inventory_management\":null,\"properties\":[],\"product_exists\":true,\"fulfillable_quantity\":1,\"grams\":0,\"price\":\"229.00\",\"total_discount\":\"5.00\",\"fulfillment_status\":null,\"price_set\":'{'\"shop_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"229.00\",\"currency_code\":\"HRK\"'}}',\"total_discount_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":['{'\"amount\":\"5.00\",\"discount_application_index\":0,\"amount_set\":'{'\"shop_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"5.00\",\"currency_code\":\"HRK\"'}}}'],\"tax_lines\":[]'}'],\"shipping_lines\":['{'\"id\":271878346596884015,\"title\":\"Generic Shipping\",\"price\":\"10.00\",\"code\":null,\"source\":\"shopify\",\"phone\":null,\"requested_fulfillment_service_id\":null,\"delivery_category\":null,\"carrier_identifier\":null,\"discounted_price\":\"10.00\",\"price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"discounted_price_set\":'{'\"shop_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}',\"presentment_money\":'{'\"amount\":\"10.00\",\"currency_code\":\"HRK\"'}}',\"discount_allocations\":[],\"tax_lines\":[]'}'],\"billing_address\":'{'\"first_name\":\"Bob\",\"address1\":\"123 Billing Street\",\"phone\":\"555-555-BILL\",\"city\":\"Billtown\",\"zip\":\"K2P0B0\",\"province\":\"Kentucky\",\"country\":\"United States\",\"last_name\":\"Biller\",\"address2\":null,\"company\":\"My Company\",\"latitude\":null,\"longitude\":null,\"name\":\"Bob Biller\",\"country_code\":\"US\",\"province_code\":\"KY\"'}',\"shipping_address\":'{'\"first_name\":\"Steve\",\"address1\":\"123 Shipping Street\",\"phone\":\"555-555-SHIP\",\"city\":\"Shippington\",\"zip\":\"40003\",\"province\":\"Kentucky\",\"country\":\"United States\",\"last_name\":\"Shipper\",\"address2\":null,\"company\":\"Shipping Company\",\"latitude\":null,\"longitude\":null,\"name\":\"Steve Shipper\",\"country_code\":\"US\",\"province_code\":\"KY\"'}',\"fulfillments\":[],\"refunds\":[],\"customer\":'{'\"id\":115310627314723954,\"email\":\"john@test.com\",\"accepts_marketing\":false,\"created_at\":null,\"updated_at\":null,\"first_name\":\"John\",\"last_name\":\"Smith\",\"orders_count\":0,\"state\":\"disabled\",\"total_spent\":\"0.00\",\"last_order_id\":null,\"note\":null,\"verified_email\":true,\"multipass_identifier\":null,\"tax_exempt\":false,\"phone\":null,\"tags\":\"\",\"last_order_name\":null,\"currency\":\"HRK\",\"accepts_marketing_updated_at\":null,\"marketing_opt_in_level\":null,\"default_address\":'{'\"id\":715243470612851245,\"customer_id\":115310627314723954,\"first_name\":null,\"last_name\":null,\"company\":null,\"address1\":\"123 Elm St.\",\"address2\":null,\"city\":\"Ottawa\",\"province\":\"Ontario\",\"country\":\"Canada\",\"zip\":\"K2H7A8\",\"phone\":\"123-123-1234\",\"name\":\"\",\"province_code\":\"ON\",\"country_code\":\"CA\",\"country_name\":\"Canada\",\"default\":true'}}}'";
	
}
