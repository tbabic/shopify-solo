package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

import org.bytepoet.shopifysolo.manager.database.DatabaseTable.IdAccessor;

interface Repository<T extends IdAccessor> {

	
	public T save(T data);
	
	public Collection<T> getAll(); 
	
	public Collection<T> getAllOrdered(Comparator<T> orderBy); 
	
	public Collection<T> getAllOrderedWhere(Comparator<T> orderBy, Function<T, Boolean> criteria);
	
	public Collection<T> getAllWhere(Function<T, Boolean> criteria);
	
	public T getSingleWhere(Function<T, Boolean> criteria);
	
	public T getById(Long id);
	
	public void deleteAll();
	
	public void deleteById(Long id);
	
	public void deleteAllWhere(Function<T, Boolean> criteria);
	
}
