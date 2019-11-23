package org.bytepoet.shopifysolo.manager.database;

import java.util.List;

import org.bytepoet.shopifysolo.manager.database.DatabaseTable.IdAccessor;

class IdSequence {

	private long value;

	IdSequence(long value) {
		this.value = value;
	}
	
	IdSequence(List<? extends IdAccessor> entries) {
		value = 0;
		for (IdAccessor entry : entries) {
			if (entry.getId() >= value) {
				value = entry.getId();
			}
		}
	}

	long nextValue() {
		this.value++;
		return this.value;
	}
}
