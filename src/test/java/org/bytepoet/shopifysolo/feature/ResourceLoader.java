package org.bytepoet.shopifysolo.feature;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class ResourceLoader {

	public static String getShopifyBankTransactionOrderJson() {
		InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream("ShopifyBankTransactionOrder.json");
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(stream, writer, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return writer.toString();
	}
	
	public static String getShopifyCorvusOrderJson() {
		InputStream stream = ResourceLoader.class.getClassLoader().getResourceAsStream("ShopifyCorvusOrder.json");
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(stream, writer, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException();
		}
		return writer.toString();
	}
}
