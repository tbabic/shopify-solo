package org.bytepoet.shopifysolo.epk.model.fields;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class EpkDate extends EpkField<Date> {

	public EpkDate(EpkValidation requirement) {
		super(8, requirement);
	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		if (super.getValue() == null) {
			return StringUtils.rightPad("", super.getLength()).toCharArray();
		}
		DateFormat format = new SimpleDateFormat("yyyyMMdd");
		return format.format(super.getValue()).toCharArray();
	}

}
