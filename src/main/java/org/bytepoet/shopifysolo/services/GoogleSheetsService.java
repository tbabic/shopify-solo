package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

@Service
public class GoogleSheetsService {
	
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	
	@Value("${google.authorization.json}")
	private String authJson;
	
	private Credential getCredentials() throws IOException {
        // Load client secrets.
		Credential credential = GoogleCredential.fromStream(new ByteArrayInputStream(authJson.getBytes()))
				.createScoped(SCOPES);
		return credential;
    }
	
	public Table<Integer, Integer, String> getData(String spreadsheetId, String range) throws Exception {
		Sheets client = createClient();
        ValueRange response = client.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
		return transform(values);
		
	}
	
	public void setData(String spreadsheetId, String range, Table<Integer, Integer, String> table) throws Exception {
		Sheets client = createClient();
        List<List<Object>> values = transform(table);
        ValueRange body = new ValueRange()
                .setValues(values);
        
        client.spreadsheets().values().update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();		
	}
	
	private Sheets createClient() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName("solofy")
                .build();
		return service;
	}
	
	private Table<Integer, Integer, String> transform(List<List<Object>> rows) {
		Table<Integer, Integer, String> table = TreeBasedTable.create();
		if(rows == null) {
			return table;
		}
		for (int i = 0; i < rows.size(); i++) {
			List<Object> row = rows.get(i);
			for (int j = 0; j < row.size(); j++) {
				table.put(i, j, row.get(j).toString());
			}
		}
		return table;
	}
	
	private List<List<Object>> transform(Table<Integer, Integer, String> table) {
		List<List<Object>> rows = new ArrayList<>();
		for(int i = 0; i< table.rowKeySet().size(); i++) {
			rows.add(new ArrayList<Object>(table.row(i).values()));
		}
		return rows;
	}

}
