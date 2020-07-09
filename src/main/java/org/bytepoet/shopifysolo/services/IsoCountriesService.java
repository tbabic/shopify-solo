package org.bytepoet.shopifysolo.services;

import java.util.Locale;

public class IsoCountriesService {

	public static String getCountryCode(String countryName) {
		String[] countryCodes = Locale.getISOCountries();
	    for (String countryCode : countryCodes) {
	        Locale localeEng = new Locale("en", countryCode);
	        Locale localeCro = new Locale("hr", countryCode);
	        if (localeEng.getDisplayCountry().equalsIgnoreCase(countryName) || localeCro.getDisplayCountry().equalsIgnoreCase(countryName)) {
	        	return countryCode;
	        }
	    }
	    throw new RuntimeException("Could not find country code for: " + countryName);
	}
}
