package org.bytepoet.shopifysolo.manager.database;

import org.bytepoet.shopifysolo.services.GoogleSheetsService;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class MockGoogleSheetsService extends GoogleSheetsService {
	
	private Table<Integer, Integer, String> data = TreeBasedTable.create();
	
	@Override
	public Table<Integer, Integer, String> getData(String spreadsheetId, String range) throws Exception {
		return data;
		
	}
	
	@Override
	public void setData(String spreadsheetId, String range, Table<Integer, Integer, String> table) throws Exception {
		this.data = table;
	}
	
	public void clear() {
		data = TreeBasedTable.create();
	}

}
