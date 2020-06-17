package org.bytepoet.shopifysolo.epk.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.bytepoet.shopifysolo.epk.model.fields.EpkField;

public class DataMerger {

	private List<EpkField<?>> fields = new ArrayList<>();
	
	public DataMerger add(EpkField<?>... fields) {
		for(EpkField<?> field:fields) {
			field.validate("invalid field");
			this.fields.add(field);
		}
		return this;
	}
	
	public char[] getData() {
		char [] data = new char[0];
		for(EpkField<?> field:fields) {
			data = ArrayUtils.addAll(data, field.getData());
		}
		return data;
	}
	
	
}
