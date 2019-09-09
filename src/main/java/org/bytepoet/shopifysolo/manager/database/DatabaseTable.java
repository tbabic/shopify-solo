package org.bytepoet.shopifysolo.manager.database;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bytepoet.shopifysolo.services.GoogleSheetsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class DatabaseTable<T> {
	
	private static final int DATA_COLUMN = 0;
	
	
	public DatabaseTable(
			String sheetId,
			GoogleSheetsService googleSheetsService,
			ObjectMapper objectMapper,
			Class<T> type) {
		this.sheetId = sheetId;
		this.googleSheetsService = googleSheetsService;
		this.objectMapper = objectMapper;
		this.type = type;
	}

	
	private List<T> entries;
	
	private Boolean cached = false;
	
	private final String sheetId;
	private final GoogleSheetsService googleSheetsService;
	private final ObjectMapper objectMapper;
	private final Class<T> type;
	
	

	public void loadDatabase() {
		synchronized(cached) {
			if (cached) {
				return;
			}
			try {
				processLoad();
				cached = true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return;
		}
	}
	
	private void processLoad() throws Exception {
		Table<Integer, Integer, String> table = googleSheetsService.getData(sheetId, "A1:A10000");
		Collection<String> stringEntries = table.column(DATA_COLUMN).values();
		if (stringEntries.isEmpty()) {
			entries = new ArrayList<>();
		} else {
			entries = new ArrayList<>(stringEntries.stream().map(v -> map(v)).collect(Collectors.toList()));
		}
		
	}
	
	public T insert(T entry) {
		loadDatabase();
		
		Table<Integer,Integer,String> table = TreeBasedTable.create();
		try {
			entries.add(entry);
			Field field = type.getDeclaredField("id");
			field.setAccessible(true);
			field.set(entry, Long.valueOf(entries.size()));
			String range = MessageFormat.format("A{0}", entries.size());
			table.put(0, 0, map(entry));
			googleSheetsService.setData(sheetId, range, table);
			return entry;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public T update(T entry) {
		loadDatabase();
		
		Table<Integer,Integer,String> table = TreeBasedTable.create();
		try {
			Field field = type.getDeclaredField("id");
			field.setAccessible(true);
			Long id = (Long) field.get(entry);
			entries.set(id.intValue()-1, entry);
			table.put(0, 0, map(entry));
			String range = MessageFormat.format("A{0}",id.intValue());
			googleSheetsService.setData(sheetId, range, table);
			return entry;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public List<T> fetchQuery(Function<T, Boolean> criteria, Comparator<T> orderBy) {
		loadDatabase();
		return entries.stream()
				.filter(entry -> {
					return criteria!= null ? criteria.apply(entry) : true; 
					})
				.sorted(orderBy).collect(Collectors.toList());
	}
	
	
	private T map(String data) {
		try {
			return objectMapper.readValue(data, type);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String map(T data) {
		try {
			return objectMapper.writeValueAsString(data);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
