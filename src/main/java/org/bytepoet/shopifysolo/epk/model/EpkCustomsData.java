package org.bytepoet.shopifysolo.epk.model;

import org.bytepoet.shopifysolo.epk.model.fields.EpkText;
import org.bytepoet.shopifysolo.manager.models.Address;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.services.IsoCountriesService;

import com.google.common.collect.ImmutableSet;

import static org.bytepoet.shopifysolo.epk.model.fields.EpkValidation.*;

import java.util.Set;

import org.bytepoet.shopifysolo.epk.model.fields.EpkInteger;

public class EpkCustomsData implements EpkMailable {

	private static final int TOTAL_LENGTH = 232;
	
	private EpkText category = new EpkText(1, value("C"));
	private EpkText receptionNumber = new EpkText(13, required());
	private EpkText content = new EpkText(3, required());
	private EpkInteger customsCategory = new EpkInteger(10, required());
	private EpkText additionalCustomsCategory = new EpkText(30, optional());
	private EpkText explanation = new EpkText(30, optional());
	private EpkText permission = new EpkText(15, optional());
	private EpkText confirmation = new EpkText(15, optional());
	private EpkText invoiceNumber = new EpkText(15, optional());
	private EpkText importerReference = new EpkText(30, optional());
	private EpkText importerPhone = new EpkText(30, optional());
	private EpkText importerAddress = new EpkText(30, optional());
	private EpkText customsNumber = new EpkText(10, optional());
	private EpkText padding = new EpkText(0, optional());
	
	public static EpkMailable createRow(String trackingNumber,
			Order order) {
		
		Address address = order.getShippingInfo();
		
		EpkCustomsData row = new EpkCustomsData();
		String countryCode = IsoCountriesService.getCountryCode(address.getCountry());
		boolean isCroatia = countryCode.equalsIgnoreCase("HR");
		Set<String> euCountryCodes = ImmutableSet.of("AT", "BE", "BG", "CY", "CZ", "DK", "EE", "FI",
				"FR", "DE", "GR", "HU", "IE", "IT", "LV", "LT", "LU", "MT", "NL", "PL",
				"PT", "RO", "SK", "SI", "ES", "SE");
		boolean isEu = euCountryCodes.contains(countryCode);
		if(isCroatia) {
			return null;
		}
		
		row.category.setValue("C");
		row.receptionNumber.setValue(trackingNumber.toUpperCase());
		if (isEu) {
			row.content.setValue("D");
		} else {
			row.content.setValue("G");
		}
		
		if(order instanceof PaymentOrder) {
			row.customsCategory.setValue(11);
			row.invoiceNumber.setValue(((PaymentOrder) order).getInvoiceNumber());
		} else {
			row.customsCategory.setValue(31);
		}
		
		return row;
	}
	
	/* (non-Javadoc)
	 * @see org.bytepoet.shopifysolo.epk.model.EpkMailable#getData()
	 */
	@Override
	public char [] getData() {
		char [] data = new DataMerger().add(
				category,
				receptionNumber,
				content,
				customsCategory ,
				additionalCustomsCategory ,
				explanation ,
				permission ,
				confirmation ,
				invoiceNumber ,
				importerReference,
				importerPhone ,
				importerAddress
				).getData();
		return data;
	}
	
}
