package org.bytepoet.shopifysolo.epk.model;

import java.util.Date;

import org.bytepoet.shopifysolo.epk.model.fields.EpkBoolean;
import org.bytepoet.shopifysolo.epk.model.fields.EpkDate;
import org.bytepoet.shopifysolo.epk.model.fields.EpkInteger;
import org.bytepoet.shopifysolo.epk.model.fields.EpkText;
import org.bytepoet.shopifysolo.epk.model.fields.EpkTime;
import org.bytepoet.shopifysolo.epk.model.fields.EpkValidation;

public class EpkFooter {
	
	private static final int TOTAL_LENGTH = 319;
	
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
	
	
	private EpkInteger fileRowsNumber = new EpkInteger(8, EpkValidation.required());
	private EpkInteger shipmentNumber = new EpkInteger(8, EpkValidation.required());
	private EpkInteger numberOfBoxes = new EpkInteger(8, EpkValidation.optional());

	public static EpkFooter createFooter(String userCode, 
			String userName, 
			String fileName,
			String postalOfficeNumber,
			int rowsNumber,
			int shipmentNumber) {
		EpkFooter footer = new EpkFooter();
		
		footer.userCode.setValue(userCode);
		footer.userName.setValue(userName);
		footer.creationDate.setValue(new Date());
		footer.fileName.setValue(fileName);
		footer.requiredTakeover.setValue(false);
		footer.postalOfficeNumber.setValue(postalOfficeNumber);
		footer.paymentType.setValue("K");
		
		footer.fileRowsNumber.setValue(rowsNumber);
		footer.shipmentNumber.setValue(shipmentNumber);
		
		
		return footer;
	}
	
	
	public char [] getData() {
		char [] data = new DataMerger().add(
				userCode, 
				userName,
				deparmentCode,
				creationDate,
				fileName,
				externalNumber,
				deliveryDate,
				deliveryTime,
				requiredTakeover,
				postalOfficeNumber,
				takeoverCity,
				takeoverStreet,
				takeoverStreetNumber,
				takeoverPhone,
				takeoverEmail,
				paymentType,
				fileRowsNumber,
				shipmentNumber,
				numberOfBoxes).getData();
		if (data.length != TOTAL_LENGTH ) {
			throw new RuntimeException("Footer must be " + TOTAL_LENGTH + " characters long");
		}
		return data;
	}
	
}
