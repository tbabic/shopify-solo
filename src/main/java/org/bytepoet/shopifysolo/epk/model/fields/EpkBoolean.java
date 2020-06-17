package org.bytepoet.shopifysolo.epk.model.fields;

public class EpkBoolean extends EpkField<Boolean> {

	public EpkBoolean(EpkValidation requirement) {
		super(1, requirement);
	}

	@Override
	public char[] getData() {
		super.validate("invalid field");
		if (super.getValue() == null) {
			return new String(" ").toCharArray();
		} else if (super.getValue()) {
			return new String("D").toCharArray();
		} else {
			return new String("N").toCharArray();
		}
	}

	

}
