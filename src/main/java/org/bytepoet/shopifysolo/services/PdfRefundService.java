package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayOutputStream;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.Currency;
import org.bytepoet.shopifysolo.manager.models.Item;
import org.bytepoet.shopifysolo.manager.models.PaymentType;
import org.bytepoet.shopifysolo.manager.models.Refund;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;


@Component
public class PdfRefundService {
	
	private static final float TOP_MARGIN = 45f;
	private static final float LEFT_MARGIN = 20f;
	@Value("${soloapi.note}")
	private String note;
	
	@Value("${soloapi.non-fiscal-note}")
	private String nonFiscalNote;
	

	public byte[] createInvoice(Refund refund) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream); 
		PdfDocument pdfDoc = new PdfDocument(writer); 
		Document document = new Document(pdfDoc);
		document.setLeftMargin(LEFT_MARGIN);
		document.setTopMargin(TOP_MARGIN);
		document.setBottomMargin(100f);
		

		
		document.add(invoiceInfo(refund));
		document.add(new Paragraph("\n"));
		document.add(invoiceItems(refund));
		document.add(new Paragraph("\n"));
		document.add(invoiceSum(refund));
		document.add(new Paragraph("\n"));
		document.add(otherInvoiceDetails(refund));
		//document.add(footer().setFixedPosition(0, 0, 1000));
		document.flush();
		
		for (int i = 1; i <= document.getPdfDocument().getNumberOfPages(); i++) {
			document.showTextAligned(footer(), 20, 20, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, 0);
		}
		
		document.close();
		return outputStream.toByteArray();
	}
	
	
	


	private IBlockElement invoiceInfo(Refund refund) {
		Table table = new Table(UnitValue.createPercentArray(new float[] {60, 40}));
		Cell logoCell = new Cell().add(createLogo()).setBorder(Border.NO_BORDER);
		table.addCell(logoCell);
		
		Cell invoiceCreationDetailsCell = new Cell().add(invoiceCreationDetails(refund)).setBorder(Border.NO_BORDER);
		table.addCell(invoiceCreationDetailsCell);
		
		Cell companyDetailsCell = companyDetails().setBorder(Border.NO_BORDER);
		table.addCell(companyDetailsCell);
		
		Cell buyerDetailsCell = buyerDetails(refund).setBorder(Border.NO_BORDER);
		buyerDetailsCell.setNextRenderer(new RoundedBorderCellRenderer(buyerDetailsCell));
		table.addCell(buyerDetailsCell);
		
		return table;
	}
	
	private Table invoiceCreationDetails(Refund refund) {
		Table table = new Table(UnitValue.createPercentArray(2));
		table.addCell(new Cell().add(new Paragraph("Račun br.").setFont(boldFont()).setFontSize(16f))
				.setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		table.addCell(new Cell().add(new Paragraph(refund.getInvoice().getNumber()).setFont(boldFont()).setFontSize(16f))
				.setBorder(Border.NO_BORDER).setPaddingLeft(10));
		
		table.addCell(new Cell().add(new Paragraph("Datum računa").setFont(font()).setFontSize(10f))
				.setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		
		DateFormat paymentDf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		paymentDf.setTimeZone(TimeZone.getTimeZone("CET"));
		
		table.addCell(new Cell().add(new Paragraph(paymentDf.format(refund.getInvoice().getDate())).setFont(font()).setFontSize(10f))
				.setBorder(Border.NO_BORDER).setPaddingLeft(10));
		
		table.addCell(new Cell().add(new Paragraph("Rok plaćanja").setFont(font()).setFontSize(10f))
				.setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));
		
		DateFormat deadlineDf = new SimpleDateFormat("dd.MM.yyyy");
		paymentDf.setTimeZone(TimeZone.getTimeZone("CET"));
		table.addCell(new Cell().add(new Paragraph(deadlineDf.format(refund.getInvoice().getDate())).setFont(font()).setFontSize(10f))
				.setBorder(Border.NO_BORDER).setPaddingLeft(10));
		return table;
	}
	
	private Cell companyDetails() {
		Cell cell = new Cell();
		cell.add(new Paragraph("Račun izdao").setFont(font()).setFontSize(10).setFontColor(WebColors.getRGBColor("dimgray")));
		cell.add(new Paragraph("IMAGINE NOW d.o.o.").setFont(boldFont()).setFontSize(12).setFontColor(DeviceRgb.BLACK));
		cell.add(new Paragraph("Trg Kralja Petra Svačića 13, 10000 Zagreb").setFont(font()).setFontSize(12).setFontColor(DeviceRgb.BLACK));
		cell.add(new Paragraph("OIB").setFont(font()).setFontSize(10)).setFontColor(WebColors.getRGBColor("dimgray"));
		cell.add(new Paragraph("85254860199").setFont(boldFont()).setFontSize(12).setFontColor(DeviceRgb.BLACK));
		return cell;
	}
	
	
	private Cell buyerDetails(Refund refund) {
		Cell cell = new Cell();
		cell.add(new Paragraph("Kupac").setFont(font()).setFontSize(10).setFontColor(WebColors.getRGBColor("dimgray")));
		cell.add(new Paragraph(refund.getOrder().getEmail()).setFont(boldFont()).setFontSize(12));
		return cell;
	}
	

	
	private Image createLogo() {
		try {
			ImageData data = ImageDataFactory.create(new ClassPathResource("logo.jpg").getURL());
			Image logo = new Image(data);
			logo.setAutoScale(false);
			logo.scale(0.5f, 0.5f);
			return logo;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private IBlockElement invoiceItems(Refund refund) {
		Table table = new Table(UnitValue.createPercentArray(new float[] {1, 17, 3, 3, 5, 5, 7, 5}));
		
		String[] headerElements = {
				"R.br.",
				"Opis proizvoda/usluge",
				"Jed.",
				"Kol.",
				"Cijena",
				"PDV",
				"Popust",
				"Iznos stavke",
		};
		
		for (int i = 0; i< headerElements.length; i++) {
			Cell headerCell = new Cell();
			headerCell.add(new Paragraph(headerElements[i]).setFont(boldFont()).setFontSize(9).setFontColor(DeviceRgb.WHITE)
					.setTextAlignment(TextAlignment.LEFT));
			headerCell.setBorder(Border.NO_BORDER);
			headerCell.setBackgroundColor(WebColors.getRGBColor("darkgray"));
			table.addHeaderCell(headerCell);
		}
		
		
		int n = 1;
		for (Item item : refund.getItems()) {
			String[] rowElements = {
					String.valueOf(n),
					item.getName(),
					"kom",
					String.valueOf(item.getQuantity()),
					price(item, refund.getOrder().getCurrency()),
					item.getTaxRate() + "%",
					discount(item, refund.getOrder().getCurrency()),
					discountAllItemsPrice(item, refund.getOrder().getCurrency()) + " €",
			};
			
			for (int i = 0; i < rowElements.length; i++) {
				Cell itemCell = new Cell();
				itemCell.add(new Paragraph(rowElements[i]).setFont(font()).setFontSize(10).setFontColor(DeviceRgb.BLACK)
						.setTextAlignment(TextAlignment.LEFT));
				itemCell.setBorder(Border.NO_BORDER);
				itemCell.setBorderBottom(new SolidBorder(WebColors.getRGBColor("darkgray"), 1.1f));
				itemCell.setMinHeight(30);
				itemCell.setKeepTogether(true);
				table.addCell(itemCell);
			}
			
			
			n++;
		}
		return table.setWidth(550);
	}
	
	private IBlockElement invoiceSum(Refund refund) {
		
		Table table = new Table(UnitValue.createPercentArray(new float[] {70f, 30f}));
		
		String sumAllItemsPrice = sumAllItemsPrice(refund);
		table.addCell(new Cell().add(new Paragraph("Ukupno:").setFont(font()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph(sumAllItemsPrice + "  €").setFont(font()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		
		
		String sumAllItemsVat = sumAllItemsVat(refund); 
		table.addCell(new Cell().add(new Paragraph("Porez (25%):").setFont(font()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph(sumAllItemsVat + "  €").setFont(font()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		
		
		String totalPrice = totalPrice(refund);
		table.addCell(new Cell().add(new Paragraph("Ukupan iznos naplate:").setFont(boldFont()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph(totalPrice + "  €").setFont(boldFont()).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		
		/*String totalPriceInKuna = totalPrice(refund, Currency.HRK);
		table.addCell(new Cell().setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph("(" + totalPriceInKuna + "  kn)").setFont(font()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		
		
		//exchange rate
		table.addCell(new Cell().add(new Paragraph("Tečaj:").setFont(font()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph("1€ = " + PdfInvoiceService.CONVERSION_RATE_STRING + " kn").setFont(font()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph("").setFont(font()).setFontSize(10).setTextAlignment(TextAlignment.RIGHT))
				.setBorder(Border.NO_BORDER));*/
		
		return table.setWidth(550);
	}
	
	
	private String price(Item item, Currency itemCurrency) {
		return convertAndFormat(-item.getPriceWithTaxRate(), itemCurrency);
	}
	
	private String discount(Item item, Currency itemCurrency) {
		if (StringUtils.isEmpty(item.getDiscount())) {
			return "0";
		}
		double discountDouble = Double.parseDouble(item.getDiscount());
		if (discountDouble == 0.0) {
			return "0";
		}
		return convertAndFormat(-item.getDiscountAmount(), itemCurrency);
	}

	private double priceWithDiscount(Item item) {
		double discount = 1;
		if (StringUtils.isNotBlank(item.getDiscount())) {
			discount = 1 - (Double.parseDouble(item.getDiscount()) / 100);
		}
		double price = 0;
		if (StringUtils.isNotBlank(item.getPrice())) {
			price = Double.parseDouble(item.getPrice()) * discount;
		}
		return price;
	}
	
	private String discountAllItemsPrice(Item item, Currency itemCurrency) {
		return convertAndFormat(-item.getTotalPrice(), itemCurrency);
	}
	
	private String sumAllItemsPrice(Refund refund) {
		try {
			double vat = getDecimalFormat().parse(sumAllItemsVat(refund)).doubleValue();
			double totalPrice =  getDecimalFormat().parse(totalPrice(refund)).doubleValue();
			double itemsPrice = totalPrice - vat;
			return getDecimalFormat().format(itemsPrice);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private String sumAllItemsVat(Refund refund) {
		//TODO: enable if everything ok
		
		/*if (refund.getInvoice().getVatAmount() != null)
		{
			return convertAndFormat(-refund.getInvoice().getVatAmount().doubleValue(), refund.getOrder().getCurrency());
		}*/
		
		double sum = 0;
		for (Item item : refund.getItems()) {
			double taxRate = Double.parseDouble(item.getTaxRate()) / 100;
			sum += taxRate * priceWithDiscount(item)*item.getQuantity();
		}
		return convertAndFormat(-sum, refund.getOrder().getCurrency());
	}
	
	private String totalPrice(Refund refund) {
		return totalPrice(refund, PdfInvoiceService.DEFAULT_CURRENCY);
	}
	
	private String totalPrice(Refund refund, Currency currency) {
		return convertAndFormat(-refund.getTotalPrice(), refund.getOrder().getCurrency(), currency);
	}
	
	private class RoundedBorderCellRenderer extends CellRenderer {
	    public RoundedBorderCellRenderer(Cell modelElement) {
	        super(modelElement);
	    }
	 
	    @Override
	    public void draw(DrawContext drawContext) {
	        drawContext.getCanvas().roundRectangle(getOccupiedAreaBBox().getX(), getOccupiedAreaBBox().getY(),
	                getOccupiedAreaBBox().getWidth(), getOccupiedAreaBBox().getHeight(), 4);
	        drawContext.getCanvas().setStrokeColor(WebColors.getRGBColor("darkgray"));
	        drawContext.getCanvas().setLineWidth(0.5f);
	        drawContext.getCanvas().stroke();
	        super.draw(drawContext);
	    }
	}
	
	private Div otherInvoiceDetails(Refund refund) {
		Div div = new Div();
		div.add(paymentType(refund));
		div.add(operator());
		div.add(note(refund));
		if (StringUtils.isNotBlank(refund.getInvoice().getJir()) || StringUtils.isNotBlank(refund.getInvoice().getZki())) { 
			div.add(fiscalization(refund));
		}
		return div;
	}
	
	private IBlockElement fiscalization(Refund refund) {
		Table table = new Table(UnitValue.createPercentArray(new float[] {50f, 50f}));
		table.addCell(new Cell().add(new Paragraph("Zaštitni kod računa:").setFont(font()).setFontSize(9).setFontColor(WebColors.getRGBColor("dimgray")))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph("Jedinstveni identifikator računa:").setFont(font()).setFontSize(9).setFontColor(WebColors.getRGBColor("dimgray")))
				.setBorder(Border.NO_BORDER));
		String zki = StringUtils.defaultString(refund.getInvoice().getZki());
		String jir = StringUtils.defaultString(refund.getInvoice().getJir());
		table.addCell(new Cell().add(new Paragraph(zki).setFont(font()).setFontSize(10))
				.setBorder(Border.NO_BORDER));
		table.addCell(new Cell().add(new Paragraph(jir).setFont(font()).setFontSize(10))
				.setBorder(Border.NO_BORDER));
		return table;
	}


	private Paragraph paymentType(Refund refund) {
		Paragraph paymentType = new Paragraph();
		paymentType.add(new Text("Način plaćanja\n").setFont(font()).setFontSize(9).setFontColor(WebColors.getRGBColor("dimgray")));
		paymentType.add(new Text(paymentTypeValue(refund.getOrder().getPaymentType())).setFont(font()).setFontSize(10));
		return paymentType;
	}
	
	private Paragraph operator() {
		Paragraph paymentType = new Paragraph();
		paymentType.add(new Text("Račun izdao / operater\n").setFont(font()).setFontSize(9).setFontColor(WebColors.getRGBColor("dimgray")));
		paymentType.add(new Text("KA").setFont(font()).setFontSize(10));
		return paymentType;
	}
	
	private Paragraph note(Refund refund) {
		Paragraph paymentType = new Paragraph();
		paymentType.add(new Text("Napomene\n").setFont(font()).setFontSize(9).setFontColor(WebColors.getRGBColor("dimgray")));
		paymentType.add(new Text(refund.getInvoice().getNote()).setFont(font()).setFontSize(10));
		return paymentType;
	}
	
	private String paymentTypeValue(PaymentType paymentType) {
		if (paymentType == PaymentType.BANK_TRANSACTION) {
			return "Transakcijski račun";
		}
		if (paymentType == PaymentType.CREDIT_CARD) {
			return "Kartice";
		}
		throw new RuntimeException("unknown payment type");
	}
	
	
	private Paragraph footer() {
		Paragraph section = new Paragraph();
		Table table = new Table(UnitValue.createPercentArray(new float[] {2.6f, 1.7f, 1.2f}));
		table.addCell(
				new Cell()
				.setBorder(Border.NO_BORDER)
				.add(new Paragraph("Naše adrese\n"
						+ "Sjedište: Trg Kralja Petra Svačića 13, 10000 Zagreb, HR")));
		table.addCell(
				new Cell()
				.setBorder(Border.NO_BORDER)
				.add(new Paragraph("Pravni podaci\n"
					+ "OIB: 85254860199 (HR85254860199)\n"
					+ "IBAN: HR9224020061100975532")));
		

		
		table.addCell(
				new Cell()
				.setBorder(Border.NO_BORDER)	
				.add(new Paragraph("Kontakti\n"
						+ "Telefon: 097 653 0447\n"
						+ "E-mail: info@kragrlica.com")));
		
		section.add(table);
	
		section.add(new Text("\n"));
		
		section.add(new Paragraph("Imagine now d.o.o. osnovana je kod Trgovačkog suda u Zagrebu (MBS 081243843). Osnivački kapital u iznosu od 20.000,00 kn uplaćen je u cijelosti.\n" + 
				"Uprava društva: Ana Kuliš."));
		
		section.setFont(font())
			.setFontSize(8)
			.setFontColor(WebColors.getRGBColor("dimgray"));
		
		return section.setWidth(550);
	}


	
	private DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator(',');
		df.setDecimalFormatSymbols(newSymbols);
		return df;
	}
	
	private static PdfFont font() {
		try {
			return PdfFontFactory.createFont(StandardFonts.HELVETICA, "Cp1250", true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private static PdfFont boldFont() {
		try {
			return PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD, "Cp1250", true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	private String convertAndFormat(double value, Currency valueCurrency) {
		return convertAndFormat(value, valueCurrency, PdfInvoiceService.DEFAULT_CURRENCY);
	}
	
	private String convertAndFormat(double value, Currency valueCurrency, Currency newCurrency) {
		if (newCurrency != valueCurrency) {
			return getDecimalFormat().format(newCurrency.convertFrom(valueCurrency, value));
		}
		return getDecimalFormat().format(value);
	}
}
