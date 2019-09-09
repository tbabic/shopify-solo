package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

interface Repository<T, ID> {

	
	public T save(T data);
	
	public Collection<T> getAll(); 
	
	public Collection<T> getAllOrdered(Comparator<T> orderBy); 
	
	public Collection<T> getAllOrderedWhere(Comparator<T> orderBy, Function<T, Boolean> criteria);
	
	public Collection<T> getAllWhere(Function<T, Boolean> criteria);
	
	public T getById(ID id);
	
}
