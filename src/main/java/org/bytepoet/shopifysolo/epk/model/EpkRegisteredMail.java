package org.bytepoet.shopifysolo.epk.model;

import org.bytepoet.shopifysolo.epk.model.fields.EpkText;
import org.bytepoet.shopifysolo.manager.models.Address;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.services.IsoCountriesService;

import static org.bytepoet.shopifysolo.epk.model.fields.EpkValidation.*;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.epk.model.fields.EpkDecimal;
import org.bytepoet.shopifysolo.epk.model.fields.EpkInteger;

public class EpkRegisteredMail implements EpkMailable {

	private static final int TOTAL_LENGTH = 368;
	
	private EpkText category = new EpkText(1, value("R"));
	private EpkText receptionNumber = new EpkText(13, required());
	private EpkText domesticInternationalTraffic = new EpkText(1, required().and(value("U").or(value("M"))));
	private EpkText externalNumber = new EpkText(30, optional());
	private EpkText isoCountry = new EpkText(2, required());
	private EpkText destinationPostalCode = new EpkText(5, requiredIfFieldEqualsValue(domesticInternationalTraffic, "U").or(empty()));
	private EpkText internationalPostalCode = new EpkText(20, requiredIfFieldEqualsValue(domesticInternationalTraffic, "M").or(empty()));
	private EpkText recepientName = new EpkText(30, required());
	private EpkText recepientLastName = new EpkText(30, required());
	private EpkText identificationNumber = new EpkText(11, optional());
	private EpkText city = new EpkText(30, requiredIfFieldEqualsValue(domesticInternationalTraffic, "M"));
	private EpkText street = new EpkText(30, optional());
	private EpkText streetNumber = new EpkText(10, optional());
	private EpkText additionalAddressInfo = new EpkText(30, optional());
	private EpkText phoneNumber = new EpkText(16, optional());
	private EpkText email = new EpkText(64, optional());
	private EpkText shipmentType = new EpkText(1, value("R"));
	private EpkText additionalServices = new EpkText(20, optional());
	private EpkInteger mass = new EpkInteger(10, required());
	private EpkDecimal redemptionValue = new EpkDecimal(10, 2, optional());
	private EpkText redemptionOrder = new EpkText(1, optional());
	private EpkText subType = new EpkText(1, optional());
	private EpkText subType2 = new EpkText(1, optional());
	private EpkText subType3 = new EpkText(1, optional());
	
	
	public static EpkMailable createRow(String trackingNumber,
			Order order) {
		Address address = order.getShippingInfo();
		
		EpkRegisteredMail row = new EpkRegisteredMail();
		String countryCode = IsoCountriesService.getCountryCode(address.getCountry());
		boolean isCroatia = countryCode.equalsIgnoreCase("HR");
		
		
		row.category.setValue("R");
		row.receptionNumber.setValue(trackingNumber.toUpperCase());
		row.domesticInternationalTraffic.setValue(isCroatia ? "U" : "M");
		row.externalNumber.setValue(order.getId().toString());
		
		row.isoCountry.setValue(countryCode);
		
		if(isCroatia) {
			row.destinationPostalCode.setValue(address.getPostalCode().replaceAll(" ", ""));
		}
		else { 
			row.internationalPostalCode.setValue(address.getPostalCode().replaceAll(" ", ""));
		}
		row.recepientName.setValue(address.getFullName().split(" ", 2)[0]);
		row.recepientLastName.setValue(address.getFullName().split(" ", 2)[1]);
		row.city.setValue(address.getCity());
		row.street.setValue(address.getStreetAndNumber());
		row.additionalAddressInfo.setValue(getCompanyAndOther(address));
		
		row.shipmentType.setValue("R");
		row.mass.setValue(60);
		
		return row;
	}
	
	private static String getCompanyAndOther(Address address) {
		if (StringUtils.isNoneBlank(address.getCompanyName(), address.getOther())) {
			return address.getCompanyName() + address.getOther();
		}
		if (StringUtils.isNotBlank(address.getCompanyName())) {
			return address.getCompanyName();
		}
		if (StringUtils.isNotBlank(address.getOther())) {
			return address.getOther();
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see org.bytepoet.shopifysolo.epk.model.EpkMailable#getData()
	 */
	@Override
	public char [] getData() {
		char [] data = new DataMerger().add(
				category,
				receptionNumber,
				domesticInternationalTraffic,
				externalNumber,
				isoCountry,
				destinationPostalCode,
				internationalPostalCode,
				recepientName,
				recepientLastName,
				identificationNumber,
				city,
				street,
				streetNumber,
				additionalAddressInfo,
				phoneNumber,
				email,
				shipmentType,
				additionalServices,
				mass,
				redemptionValue,
				redemptionOrder,
				subType,
				subType2,
				subType3).getData();
		if (data.length != TOTAL_LENGTH ) {
			throw new RuntimeException("Reg. mail must be " + TOTAL_LENGTH + " characters long");
		}
		return data;
	}
	
}
