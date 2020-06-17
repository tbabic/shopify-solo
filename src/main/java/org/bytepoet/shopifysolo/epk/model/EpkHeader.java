package org.bytepoet.shopifysolo.epk.model;

import java.util.Date;

import org.bytepoet.shopifysolo.epk.model.fields.EpkBoolean;
import org.bytepoet.shopifysolo.epk.model.fields.EpkDate;
import org.bytepoet.shopifysolo.epk.model.fields.EpkValidation;
import org.bytepoet.shopifysolo.epk.model.fields.EpkText;
import org.bytepoet.shopifysolo.epk.model.fields.EpkTime;

public class EpkHeader {
	
	private static final int TOTAL_LENGTH = 295;

	private EpkText userCode = new EpkText(8, EpkValidation.required());
	private EpkText userName = new EpkText(50, EpkValidation.required());
	private EpkText deparmentCode = new EpkText(5, EpkValidation.optional());
	private EpkDate creationDate = new EpkDate(EpkValidation.required());
	private EpkText fileName = new EpkText(40, EpkValidation.required());
	private EpkText externalNumber = new EpkText(20, EpkValidation.optional());
	private EpkDate deliveryDate = new EpkDate(EpkValidation.optional());
	private EpkTime deliveryTime = new EpkTime(4, EpkValidation.optional());
	private EpkBoolean requiredTakeover = new EpkBoolean(EpkValidation.required());
	private EpkText postalOfficeNumber = new EpkText(5, EpkValidation.required());
	private EpkText takeoverCity = new EpkText(30, EpkValidation.requiredIfFieldEqualsValue(requiredTakeover, true));
	private EpkText takeoverStreet = new EpkText(30, EpkValidation.requiredIfFieldEqualsValue(requiredTakeover, true));
	private EpkText takeoverStreetNumber = new EpkText(5, EpkValidation.requiredIfFieldEqualsValue(requiredTakeover, true));
	private EpkText takeoverPhone = new EpkText(16, EpkValidation.requiredIfFieldEqualsValue(requiredTakeover, true));
	private EpkText takeoverEmail = new EpkText(64, EpkValidation.optional());
	private EpkText paymentType = new EpkText(1, EpkValidation.required());
	
	
	
	public static EpkHeader createHeader(String userCode, 
			String userName, 
			String fileName,
			String postalOfficeNumber) {
		EpkHeader header = new EpkHeader();
		
		header.userCode.setValue(userCode);
		header.userName.setValue(userName);
		header.creationDate.setValue(new Date());
		header.fileName.setValue(fileName);
		header.requiredTakeover.setValue(false);
		header.postalOfficeNumber.setValue(postalOfficeNumber);
		header.paymentType.setValue("K");
		
		
		return header;
	}
	
	
	public char [] getData() {
		char [] data = new DataMerger().add(
				userCode.validate("userCode"), 
				userName.validate("userName"),
				deparmentCode.validate("deparmentCode"),
				creationDate.validate("creationDate"),
				fileName.validate("fileName"),
				externalNumber.validate("externalNumber"),
				deliveryDate.validate("deliveryDate"),
				deliveryTime.validate("deliveryTime"),
				requiredTakeover.validate("requiredTakeover"),
				postalOfficeNumber.validate("postalOfficeNumber"),
				takeoverCity.validate("takeoverCity"),
				takeoverStreet.validate("takeoverStreet"),
				takeoverStreetNumber.validate("takeoverStreetNumber"),
				takeoverPhone.validate("takeoverPhone"),
				takeoverEmail.validate("takeoverEmail"),
				paymentType.validate("paymentType")).getData();
		if (data.length != TOTAL_LENGTH ) {
			throw new RuntimeException("Header must be " + TOTAL_LENGTH + " characters long");
		}
		return data;
	}
	
}
