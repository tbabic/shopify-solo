package org.bytepoet.shopifysolo.manager.models;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CsvBuilder<T> {

	private List<String> headers;
	private List<T> dataObjects;
	
	private List<Function<T, String>> fieldFunctions;
	
	private String encoding = "windows-1250";
	
	private ByteArrayOutputStream outputStream;
	
	public CsvBuilder<T> setDataObjects(List<T> dataObjects) {
		this.dataObjects = dataObjects;
		return this;
	}
	
	public CsvBuilder<T> setHeaders(List<String> headers) {
		this.headers = headers;
		return this;
		
	}
	
	public CsvBuilder<T> setHeaders(String... headers) {
		this.headers = Arrays.asList(headers);
		return this;
		
	}
	
	public CsvBuilder<T> addHeader(String header) {
		if (this.headers == null) {
			this.headers = new ArrayList<>();
		}
		this.headers.add(header);
		return this;
	}
	
	public CsvBuilder<T> addHeaderAndField(String header, Function<T, String> function) {
		if (this.headers == null) {
			this.headers = new ArrayList<>();
		}
		if (this.fieldFunctions == null) {
			this.fieldFunctions = new ArrayList<>();
		}
		this.headers.add(header);
		this.fieldFunctions.add(function);
		
		return this;
	}
	
	
	public CsvBuilder<T> addField(Function<T, String> function) {
		if (this.fieldFunctions == null) {
			this.fieldFunctions = new ArrayList<>();
		}
		this.fieldFunctions.add(function);
		
		return this;
	}
	
	public CsvBuilder<T> setEncoding(String encoding) {
		this.encoding = encoding;
		return this;
	}
	
	
	public byte[] build() {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			this.outputStream = outputStream;
			this.writeHeaderRow();
			
			for (T dataObject : this.dataObjects) {
				addNewLine();
				writeDataRow(dataObject);
			}
			return this.outputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	
	private void writeHeaderRow() {
		boolean first = true;
		for (String header : headers) {
			if (!first) {
				addSeparator();
			}
			first = false;
			addToStream(header);
		}
	}
	
	
	private void writeDataRow(T dataObject) {
		boolean first = true;
		for (Function<T, String> function : fieldFunctions) {
			if (!first) {
				addSeparator();
			}
			first = false;
			String data = function.apply(dataObject);
			addToStream(data);
		}
	}
	
	private void addToStream(String string) {
		try {
			outputStream.write(string.getBytes(this.encoding));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
	private void addNewLine() {
		addToStream("\r\n");
	}
	
	private void addSeparator() {
		addToStream(";");
	}
	
}
