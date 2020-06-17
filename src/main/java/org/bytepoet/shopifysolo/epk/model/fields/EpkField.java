package org.bytepoet.shopifysolo.epk.model.fields;

public abstract class EpkField<T> {

	private int length;
	
	private EpkValidation validation;
	
	private T value;

	public EpkField(int length, EpkValidation validation) {
		this.length = length;
		this.validation = validation;
	}
	
	public EpkField<T> validation(EpkValidation validation) {
		this.validation = validation;
		return this;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public abstract char [] getData();

	protected int getLength() {
		return length;
	}

	public T getValue() {
		return value;
	}

	public EpkValidation getRequired() {
		return validation;
	}
	
	public boolean isValid() {
		return validation.isValid(this);
	}
	
	public EpkField<T> validate(String message) {
		if (!isValid()) {
			throw new RuntimeException(message);
		}
		return this;
	}
	
	
	boolean isEmpty() {
		return value == null;
	}
	
	
	
	
}
