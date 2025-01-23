package org.bytepoet.shopifysolo.manager.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public enum Currency {

	HRK("1", "kn"),
	EUR("7,53450", "€"),
	INCORRECT_EUR("7,534550", "€");
	
	private final String value;
	private final String symbol;
	private final BigDecimal decimalValue;

	

	private Currency(String value, String symbol) {
		this.value = value;
		this.symbol = symbol;
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		DecimalFormat decimalFormat = new DecimalFormat();
		decimalFormat.setDecimalFormatSymbols(symbols);
		decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
		decimalFormat.setParseBigDecimal(true);
		try {
			decimalValue = (BigDecimal) decimalFormat.parse(value);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public String getValue() {
		return value;
	}
	
	public BigDecimal getDecimalValue() {
		return decimalValue;
	}
	
	public double convertFrom(Currency currency, double value) {
		return convert(currency, this, value);
	}
	
	public double convertTo(Currency currency, double value) {
		return convert(this, currency, value);
	}
	
	public BigDecimal convertFrom(Currency currency, BigDecimal value) {
		return convert(currency, this, value);
	}
	
	public BigDecimal convertTo(Currency currency, BigDecimal value) {
		return convert(this, currency, value);
	}
	
	private double convert(Currency sourceCurrency, Currency targetCurrency, double value) {
		if (sourceCurrency.decimalValue.doubleValue() == targetCurrency.decimalValue.doubleValue()) {
			return value;
		}
		double multiplier = sourceCurrency.decimalValue.doubleValue() / targetCurrency.decimalValue.doubleValue();
		return value * multiplier;
	}
	
	private BigDecimal convert(Currency sourceCurrency, Currency targetCurrency, BigDecimal value) {
		if (sourceCurrency.decimalValue.equals(targetCurrency.decimalValue)) {
			return value;
		}
		return sourceCurrency.decimalValue
				.divide(targetCurrency.decimalValue)
				.multiply(value);
		
	}
}
