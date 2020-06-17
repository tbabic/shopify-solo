package org.bytepoet.shopifysolo.epk.model.fields;

import org.apache.commons.lang3.StringUtils;

public class EpkInteger extends EpkField<Integer> {

	public EpkInteger(int length, EpkValidation validation) {
		super(length, validation);
	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		if(super.getValue() == null) {
			return StringUtils.rightPad("", super.getLength()).replace(" ", "0").toCharArray();
		}
		String value = Integer.toString(super.getValue());
		if (value.length() > super.getLength()) {
			throw new RuntimeException("Dozvoljena dužina: " + getLength() + " za broj: " + super.getValue());
		}
		value = StringUtils.leftPad(value, super.getLength()).replace(" ", "0");
		return value.toCharArray();
	}

}
