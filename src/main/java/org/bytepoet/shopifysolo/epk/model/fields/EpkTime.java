package org.bytepoet.shopifysolo.epk.model.fields;

import org.apache.commons.lang3.StringUtils;

public class EpkTime extends EpkField<String> {

	public EpkTime(int length, EpkValidation requirement) {
		super(length, requirement);

	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		if (super.getValue() == null) {
			return StringUtils.rightPad("", super.getLength()).toCharArray();
		}
		if (super.getValue().length() != super.getLength()) {
			throw new RuntimeException("Dozvoljena dužina: " + getLength() + " za niz: " + super.getValue());
		}
		try {
			Integer.parseInt(super.getValue());
		} catch (NumberFormatException e) {
			throw new RuntimeException("Vrijeme nije broj: " + super.getLength(), e);
		}
		return super.getValue().toCharArray();
	}

}
