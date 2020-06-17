package org.bytepoet.shopifysolo.epk.model.fields;

import org.apache.commons.lang3.StringUtils;

public class EpkDecimal extends EpkField<Double> {

	private int decimalLength; 
	
	public EpkDecimal(int length, int decimalLength, EpkValidation requirement) {
		super(length, requirement);
	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		if(super.getValue() == null) {
			return StringUtils.rightPad("", super.getLength()).replace(" ", "0").toCharArray();
		}
		if (decimalLength < 0) {
			throw new RuntimeException("Broj decimalnih polja mora biti veći od 0");
		}
		double factor = (long) Math.pow(10, decimalLength);
		String value = Long.toString( Math.round(super.getValue() * factor));
		if (value.length() > super.getLength()) {
			throw new RuntimeException("Dozvoljena dužina: " + getLength() + " za decimalni broj: " + super.getValue());
		}
		value = StringUtils.leftPad(value, super.getLength()).replace(" ", "0");
		return value.toCharArray();
	}

}
