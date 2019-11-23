package org.bytepoet.shopifysolo.manager.database;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bytepoet.shopifysolo.services.GoogleSheetsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class DatabaseTable<T extends DatabaseTable.IdAccessor> {
	
	private static final int DATA_COLUMN = 0;
	
	private IdSequence sequence;
	
	
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

	
	private ArrayList<T> entries;
	
	private Map<Long, Long> idRowMap = new HashMap<>();
	
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
		sequence = new IdSequence(entries);
		for (int i = 0; i < entries.size(); i++) {
			idRowMap.put(entries.get(i).getId(), (long) i);
		}
		
	}
	
	public T insert(T entry) {
		loadDatabase();
		
		Table<Integer,Integer,String> table = TreeBasedTable.create();
		entry.setId(sequence.nextValue());
		String range = MessageFormat.format("A{0}", String.valueOf(entries.size()+1));
		table.put(0, DATA_COLUMN, map(entry));
		try {			
			googleSheetsService.setData(sheetId, range, table);
			entries.add(entry);
			idRowMap.put(entry.getId(), 1L + entries.size());
			return entry;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public T update(T entry) {
		loadDatabase();
		
		Table<Integer,Integer,String> table = TreeBasedTable.create();
		table.put(0, 0, map(entry));
		if (!idRowMap.containsKey(entry.getId())) {
			throw new RuntimeException("Can not update: no element with specified id");
		}
		long rowNum = idRowMap.get(entry.getId());
		entries.set((int) (rowNum-1), entry);
		
		String range = MessageFormat.format("A{0}",String.valueOf(rowNum));
		try {
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
	
	public void deleteQuery(Function<T, Boolean> criteria) {
		loadDatabase();
		List<T> entriesToDelete = fetchQuery(criteria, null);
		List<Long> idRowsToRemove = entriesToDelete.stream().map(e -> e.getId()).collect(Collectors.toList());
		List<Long> rowsToDelete = entriesToDelete.stream().map(e -> idRowMap.get(e.getId())).sorted().collect(Collectors.toList());
		long startRow = rowsToDelete.get(0);
		long endRow = entries.size();
		ArrayList<T> copiedEntries = new ArrayList<>(entries);
		
		for (int i = rowsToDelete.size()-1; i >= 0; i--) {
			copiedEntries.remove(rowsToDelete.get(i).intValue()-1);
		}
		
		List<T> entriesToUpdate = copiedEntries.subList((int) (startRow-1), copiedEntries.size());
		
		Table<Integer,Integer,String> table = TreeBasedTable.create();
		int i = 0;
		for(T entry : entriesToUpdate) {
			table.put(i, DATA_COLUMN, map(entry));
			i++;
		}
		String range = MessageFormat.format("A{0}:A{1}",String.valueOf(startRow), String.valueOf(endRow));
		try {
			googleSheetsService.setData(sheetId, range, table);
			entries.clear();
			entries = copiedEntries;
			idRowMap.keySet().removeAll(idRowsToRemove);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
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
	
	public static abstract class IdAccessor {
		
		abstract public Long getId();

		abstract protected void setId(Long id);
	}

	
}
