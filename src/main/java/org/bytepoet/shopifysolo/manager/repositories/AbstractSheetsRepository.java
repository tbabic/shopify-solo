package org.bytepoet.shopifysolo.manager.repositories;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.bytepoet.shopifysolo.manager.database.DatabaseTable;
import org.bytepoet.shopifysolo.manager.database.DatabaseTable.IdAccessor;
import org.bytepoet.shopifysolo.services.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

public abstract class AbstractSheetsRepository<T extends IdAccessor> implements Repository<T>{
	
	private String sheetId;
	
	AbstractSheetsRepository(String sheetId) {
		this.sheetId = sheetId;
	}

	@Autowired
	private GoogleSheetsService googleSheetsService;
	
	@Autowired
	private MappingJackson2HttpMessageConverter springMvcJacksonConverter;
	
	private DatabaseTable<T> table;

	@PostConstruct
	public void init() {
		table = new DatabaseTable<T>(sheetId, googleSheetsService, springMvcJacksonConverter.getObjectMapper(), getType());
	}
	
	@Override
	public T save(T data) {
		Long id = data.getId();
		if (getById(id) != null) {
			return table.update(data);
		}
		return table.insert(data);
	}

	@Override
	public List<T> getAll() {
		return table.fetchQuery(entry -> true, getDefaultComparator());
	}
	
	@Override
	public List<T> getAllOrdered(Comparator<T> orderBy) {
		return table.fetchQuery(entry -> true, orderBy);
	}

	@Override
	public List<T> getAllOrderedWhere(Comparator<T> orderBy, Function<T, Boolean> criteria) {
		return table.fetchQuery(criteria, orderBy);
	}

	@Override
	public List<T> getAllWhere(Function<T, Boolean> criteria) {
		return table.fetchQuery(criteria, getDefaultComparator());
	}
	
	@Override
	public T getSingleWhere(Function<T, Boolean> criteria) {
		List<T> results = getAllWhere(criteria);
		if (results.size() > 1) {
			throw new RuntimeException("More than one result for specified criteria.");
		}
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	@Override
	public T getById(Long id) {
		List<T> results = table.fetchQuery(data -> data.getId().equals(id) , getDefaultComparator());
		if (results.size() > 1) {
			throw new RuntimeException("found multiple elements by id: " + id.toString());
		}
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@Override
	public void deleteAll() {
		table.deleteQuery(entry -> true);
		
	}

	@Override
	public void deleteById(Long id) {
		table.deleteQuery(data -> data.getId().equals(id));
		
	}

	@Override
	public void deleteAllWhere(Function<T, Boolean> criteria) {
		table.deleteQuery(criteria);
		
	}
	
	
	protected abstract Class<T> getType();
	
	
	protected Comparator<T> getDefaultComparator() {
	    return (a1, a2) -> 0;
	}
		

}
