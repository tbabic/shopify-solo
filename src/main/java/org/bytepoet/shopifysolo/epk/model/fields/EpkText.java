package org.bytepoet.shopifysolo.epk.model.fields;

import org.apache.commons.lang3.StringUtils;

public class EpkText extends EpkField<String> {
	
	public EpkText(int length, EpkValidation requirement) {
		super(length, requirement);
	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		String value = StringUtils.isBlank(super.getValue()) ? "" : super.getValue();
		value = StringUtils.rightPad(value, super.getLength());
		return value.substring(0, super.getLength()).toCharArray();
	}

}
