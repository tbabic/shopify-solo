package org.bytepoet.shopifysolo.epk.model.fields;

import java.util.function.Predicate;;

public class EpkValidation {

	private Predicate<EpkField<?>> condition;
	
	private EpkValidation(Predicate<EpkField<?>> condition) {
		this.condition = condition;
	}


	public boolean isValid(EpkField<?> field) {
		return condition.test(field);
	}
	
	public EpkValidation or(EpkValidation validation) {
		return new EpkValidation(field -> (isValid(field) || validation.isValid(field)));
	}
	
	public EpkValidation and(EpkValidation validation) {
		return new EpkValidation(field -> (isValid(field) && validation.isValid(field)));
	}
	
	public EpkValidation not() {
		return new EpkValidation(field -> !isValid(field));
	}
	

	public static EpkValidation required() {
		return new EpkValidation(field ->  !field.isEmpty());
	}
	
	public static EpkValidation empty() {
		return new EpkValidation(field ->  field.isEmpty());
	}
	
	public static EpkValidation optional() {
		return new EpkValidation(field -> true );
	}
	
	public static EpkValidation value(Object value) {
		return new EpkValidation(field ->  !field.isEmpty() && field.getValue().equals(value));
	}
	
	public static EpkValidation conditional(Predicate<EpkField<?>> condition) {
		return new EpkValidation(condition);
	}
	
	public static EpkValidation requiredIfFieldNotEmpty(EpkField<?> otherField) {
		return new EpkValidation(field -> otherField.isEmpty() || !field.isEmpty());
	}
	
	public static <T> EpkValidation requiredIfFieldEqualsValue(EpkField<T> otherField, T value) {
		return new EpkValidation(field -> {
			return otherField.isEmpty() || !otherField.getValue().equals(value) || !field.isEmpty();
		});
	}
	
}
