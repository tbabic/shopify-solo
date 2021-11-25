package org.bytepoet.shopifysolo.epk.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class EpkLocations {

	private static Map<String, Map<String, String>> locationsMap = new TreeMap<String, Map<String, String>>();
	
	private static void loadFile() {
		File file = new File(EpkLocations.class.getResource("popis_naselja.csv").getFile());
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    for(String line; (line = br.readLine()) != null; ) {
		        String[] lineSplitted = line.split(";");
		        
		        String code = lineSplitted[0];
		        String place = lineSplitted[1].toLowerCase();
		        String postalCode = lineSplitted[2];
		        
		        Map<String, String> codePlaceMap = locationsMap.get(postalCode);
		        if (codePlaceMap == null) {
		        	codePlaceMap = new TreeMap<String, String>();
		        	locationsMap.put(postalCode, codePlaceMap);
		        }
		        codePlaceMap.put(place, code);
		        
		        
		    }

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static String getCode(String postalCode, String place) {
		if (locationsMap.isEmpty()) {
			loadFile();
		}
		return locationsMap.get(postalCode).get(place);
	}
	
	
	
}
