package org.bytepoet.shopifysolo.epk.model;

import org.bytepoet.shopifysolo.epk.model.fields.EpkText;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import static org.bytepoet.shopifysolo.epk.model.fields.EpkValidation.*;

import org.bytepoet.shopifysolo.epk.model.fields.EpkDecimal;
import org.bytepoet.shopifysolo.epk.model.fields.EpkInteger;

public class EpkCustomsNonEuAdditionalData implements EpkMailable {

	private static final int TOTAL_LENGTH = 131;
	
	private EpkText category = new EpkText(1, value("S"));
	private EpkText receptionNumber = new EpkText(13, required());
	private EpkText contentDescription = new EpkText(30, required());
	private EpkInteger quantity = new EpkInteger(30, required());
	private EpkInteger totalMass = new EpkInteger(10, required());
	private EpkDecimal totalValue = new EpkDecimal(10, 2, required());
	private EpkText currency = new EpkText(5, required());
	private EpkText tarrifNumber = new EpkText(30, optional());
	private EpkText sourceCountry = new EpkText(2, optional());	
	
	public static EpkMailable createRow(String trackingNumber,
			Order order) {
		
		EpkCustomsNonEuAdditionalData row = new EpkCustomsNonEuAdditionalData();
		
		row.category.setValue("S");
		row.receptionNumber.setValue(trackingNumber.toUpperCase());
		row.contentDescription.setValue("Brass Jewelry");
		int quantity = 0;
		for (Item item : order.getItems()) {
			quantity += item.getQuantity();
		}
		row.quantity.setValue(quantity);
		
		row.totalMass.setValue(quantity*15);

		
		if(order instanceof PaymentOrder) {
			row.totalValue.setValue(((PaymentOrder) order).getTotalPrice());
		} else {
			row.totalValue.setValue(0.01);
		}
		
		row.currency.setValue("HRK");
		row.sourceCountry.setValue("HR");
		
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
				contentDescription,
				quantity ,
				totalMass ,
				totalValue,
				currency ,
				tarrifNumber ,
				sourceCountry).getData();
		if (data.length != TOTAL_LENGTH ) {
			throw new RuntimeException("Additional customs data must be " + TOTAL_LENGTH + " characters long");
		}
		return data;
	}
	
}
