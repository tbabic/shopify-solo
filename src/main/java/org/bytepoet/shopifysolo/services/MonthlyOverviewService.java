package org.bytepoet.shopifysolo.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bytepoet.shopifysolo.manager.models.Currency;
import org.bytepoet.shopifysolo.manager.models.Invoice;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.OrderArchive;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.bytepoet.shopifysolo.manager.models.RefundInvoice;
import org.bytepoet.shopifysolo.manager.repositories.OrderArchiveRepository;
import org.bytepoet.shopifysolo.manager.repositories.OrderRepository;
import org.bytepoet.shopifysolo.manager.repositories.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MonthlyOverviewService {
	
	private static Currency CURRENCY = Currency.EUR;

	@Value("${soloapi.shipping-title}")
	private String shippingTitle;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private RefundRepository refundRepository;
	
	@Autowired
	private OrderArchiveRepository orderArchiveRepository;
	
	private static class OverviewInvoice {
		String invoiceNumber;
		String invoiceDate;
		String paymentDate;
		String amount;
		String shipping;
		String paymentType;
		
		public OverviewInvoice(PaymentOrder order, String shippingTitle) {
			this.invoiceNumber = order.getInvoiceNumber();
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			this.invoiceDate = df.format(order.getPaymentDate());
			this.paymentDate = df.format(order.getPaymentDate());
			this.amount = getDecimalFormat().format(order.getTotalPrice(CURRENCY));
			Optional<Item> shippingItem = order.getItems().stream().filter(i -> i.getName().equalsIgnoreCase(shippingTitle)).findFirst();
			this.shipping = "0,00";
			if (shippingItem.isPresent()) {
				this.shipping = getDecimalFormat().format(CURRENCY.convertFrom(order.getCurrency(), shippingItem.get().getTotalPrice()));
			}
			this.paymentType = mapPaymentType(order.getPaymentType());
			
			
		}
		
		public OverviewInvoice(Refund refund, String shippingTitle) {
			this.invoiceNumber = refund.getInvoice().getNumber();
			DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			this.invoiceDate = df.format(refund.getInvoice().getDate());
			this.paymentDate = df.format(refund.getInvoice().getDate());
			this.amount = getDecimalFormat().format(-refund.getTotalPrice(CURRENCY));
			Optional<Item> shippingItem = refund.getItems().stream().filter(i -> i.getName().equalsIgnoreCase(shippingTitle)).findFirst();
			this.shipping = "-0,00";
			if (shippingItem.isPresent()) {
				this.shipping = getDecimalFormat().format(CURRENCY.convertFrom(refund.getOrder().getCurrency(), -shippingItem.get().getTotalPrice()));
			}
			this.paymentType = mapPaymentType(refund.getOrder().getPaymentType());
		}
		
		private static DecimalFormat getDecimalFormat() {
			DecimalFormat df = new DecimalFormat("0.00");
			df.setRoundingMode(RoundingMode.HALF_UP);
			DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
			newSymbols.setDecimalSeparator(',');
			df.setDecimalFormatSymbols(newSymbols);
			return df;
		}
		
		private static String mapPaymentType(PaymentType paymentType) {
			if (paymentType == PaymentType.BANK_TRANSACTION) {
				return "TK";
			} else {
				return "K";
			}
		}
	}
	
	public String createYearlyOverviewFile(int year, boolean useArchive) {
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Zagreb")));
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		cal.add(Calendar.YEAR, 1);
		cal.add(Calendar.MILLISECOND, -1);
		Date end = cal.getTime();
		
		return createOverviewFile(start, end, useArchive);		
				
	}
	
	
	public String createMonthlyOverviewFile(int month, int year, boolean useArchive) {
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Zagreb")));
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month-1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.MILLISECOND, -1);
		Date end = cal.getTime();
		
		return createOverviewFile(start, end, useArchive);		
				
	}


	private String createOverviewFile(Date start, Date end, boolean useArchive) {
		
		
		List<PaymentOrder> paymentOrders = new ArrayList<PaymentOrder>();
		if (useArchive) {
			OrderArchive orderArchive = orderArchiveRepository.findAll().stream().findFirst().orElse(new OrderArchive());
			List<PaymentOrder> archived = orderArchive.getOrders().stream().filter((order) -> {
				if (order instanceof PaymentOrder) {
					if (((PaymentOrder) order).isPaid() && ((PaymentOrder) order).getPaymentDate() != null && 
							((PaymentOrder) order).getPaymentDate().after(start) && ((PaymentOrder) order).getPaymentDate().before(end)) {
						return true;
					}
				}
				return false;
			}).map(o -> ((PaymentOrder) o)).collect(Collectors.toList());
			paymentOrders.addAll(archived);
		}
		
		List<PaymentOrder> respositoryOrders = orderRepository.getByPamentDateBetween(start, end);
		List<Refund> refunds = refundRepository.getByInvoiceDateBetween(start, end);
		
		
		
		paymentOrders.addAll(respositoryOrders);
		
		List<OverviewInvoice> overviewInvoices = Stream
				.concat(paymentOrders.stream().map(o -> new OverviewInvoice(o, shippingTitle)), 
						refunds.stream().map(r -> new OverviewInvoice(r, shippingTitle)))
				.sorted((a,b) -> {
					int lengthCompare = Integer.compare(a.invoiceNumber.length(), b.invoiceNumber.length());
					if (lengthCompare == 0) {
						return a.invoiceNumber.compareTo(b.invoiceNumber);
					}
					return lengthCompare;
					
				})
				.collect(Collectors.toList());
		
		double totalIncome = paymentOrders.stream().collect(Collectors.summingDouble(po -> po.getTotalPrice(CURRENCY)))
				- refunds.stream().collect(Collectors.summingDouble(r -> r.getTotalPrice(CURRENCY)));
		
		double creditCardIncome = paymentOrders.stream().filter(po -> po.getPaymentType() == PaymentType.CREDIT_CARD ).collect(Collectors.summingDouble(po -> po.getTotalPrice(CURRENCY)))
				- refunds.stream().filter(r -> r.getOrder().getPaymentType() == PaymentType.CREDIT_CARD ).collect(Collectors.summingDouble(r -> r.getTotalPrice(CURRENCY)));
		
		double bankIncome = paymentOrders.stream().filter(po -> po.getPaymentType() == PaymentType.BANK_TRANSACTION ).collect(Collectors.summingDouble(po -> po.getTotalPrice(CURRENCY)))
				- refunds.stream().filter(r -> r.getOrder().getPaymentType() == PaymentType.BANK_TRANSACTION ).collect(Collectors.summingDouble(r -> r.getTotalPrice(CURRENCY)));
		
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("Broj RN;");
		builder.append("Datum RN;");
		builder.append("Datum dospijeća;");
		builder.append("Iznos RN;");
		builder.append("poštarina;");
		builder.append("Način plaćanja;");
		builder.append("Transackije ukupno");
		builder.append("\n");
		
		boolean bankIncomePrinted = false;
		
		for (OverviewInvoice o : overviewInvoices) {
			builder.append(o.invoiceNumber);
			builder.append(";");
			builder.append(o.invoiceDate);
			builder.append(";");
			builder.append(o.paymentDate);
			builder.append(";");
			builder.append(o.amount);
			builder.append(";");
			builder.append(o.shipping);
			builder.append(";");
			builder.append(o.paymentType);
			
			if (!bankIncomePrinted) {
				builder.append(";");
				builder.append(OverviewInvoice.getDecimalFormat().format(bankIncome));
				bankIncomePrinted = true;
			}
			
			builder.append("\n");
		}
			
		return builder.toString();
	}
}
