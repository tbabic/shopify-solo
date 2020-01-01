package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.bytepoet.shopifysolo.print.models.Base64Wrapper;
import org.bytepoet.shopifysolo.print.models.PostalFormAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class PrintAddressService {

	// total width 150 mm
	// 49 mm from top
	// header height: 14 mm
	// 3 mm from left
	// row height 9.5 mm
	// first column width:5mm
	// second column width: 34 mm
	// third column width: 46.55 mm
	// fourth column with: 45 mm
	// fifth column width: 20 mm
	
	private static final float TOTAL_WIDTH = 147f;
	private static final Float[] COLUMN_WIDTHS_IN_MM = {5f, 34f, 46f, 45f, 20f};
	private static final float MAX_FONT_SIZE = 10;
	private static final float ROW_HEIGHT = 9.7f;
	private static final float TOP_MARGIN = 59.8f;
	private static final float LEFT_MARGIN = 15.9f;
	
	@Autowired
	private EncryptionService encryptionService;
	
	public Base64Wrapper printToPostalFormPdf(List<? extends PostalFormAddress> addressList) throws Exception {
		Document document = new Document(PageSize.A4.rotate(), mmToUnit(LEFT_MARGIN), 10f, mmToUnit(TOP_MARGIN), 0f);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		
		byte[] background;
		
		try (InputStream input = new ClassPathResource("prijamna_knjiga_encrypted.dat").getInputStream();
				ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			
			encryptionService.decrypt(input, output);
			background = output.toByteArray();
		}
			
		writer.setPageEvent(new PDFBackground(background));
		 
		document.open();
		for (int start = 0, page = 1; start < addressList.size(); start+=10, page++) {
			int end = start+10;
			if (addressList.size() < end) {
				end = addressList.size();
			}
			List<? extends PostalFormAddress> adressesOnPage = addressList.subList(start, end);
			addPage(document, adressesOnPage, page);
		}
		
		
		document.close();
		outputStream.toByteArray();
		
		Base64Wrapper base64Wrapper = new Base64Wrapper();
		base64Wrapper.setValue(Base64.encodeBase64String(outputStream.toByteArray()));
		return base64Wrapper;
		
	}
	
	
	
	private void addPage(Document document, List<? extends PostalFormAddress> adressesOnPage, int page) throws Exception {
		document.newPage();
		List<Float> widthList = Arrays.asList(COLUMN_WIDTHS_IN_MM).stream().map(f -> mmToUnit(f)).collect(Collectors.toList());
		float[] columnWidths = ArrayUtils.toPrimitive(widthList.toArray(new Float[0]));
		
		PdfPTable table = new PdfPTable(columnWidths);
		table.setHorizontalAlignment(Element.ALIGN_LEFT);
		table.setTotalWidth(mmToUnit(TOTAL_WIDTH));
		table.setLockedWidth(true);
		for (int i = 0; i<adressesOnPage.size(); i++) {
			addRow(table, adressesOnPage.get(i), (page-1)*10+i+1);
		}
		
		document.add(table);
	}
	
	
	private void addRow(PdfPTable table, PostalFormAddress address, int rowNumber) {
	    addToTable(table, new Integer(rowNumber).toString(), 5, ROW_HEIGHT);
	    addToTable(table, "", 34f, ROW_HEIGHT);
	    
	    addToTable(table, address.getFullRecepient(), 46, ROW_HEIGHT);
	    addToTable(table, address.getFullAddress(), 45, ROW_HEIGHT);
	    addToTable(table, address.getFullDestination(), 20, ROW_HEIGHT);
	}
	
	
	private static float mmToUnit(float milimiters) {
		return 72.0f * milimiters / 25.4f; 
	}
	
	private void addToTable(PdfPTable table, String text, float cellWidth, float cellHeight) {
		 PdfPCell cell = new PdfPCell(table.getDefaultCell());
		 cell.setFixedHeight(mmToUnit(cellHeight));
		 cell.setPhrase(createPhrase(text, cellWidth, cellHeight));
		 cell.setPadding(1f);
		 cell.setBorderColor(BaseColor.RED);
		 cell.setBorderWidth(0);
		 table.addCell(cell);
		 
	}
	
	private Phrase createPhrase(String text, float cellWidth, float cellHeight) {
		float fontSize = cellFontSize(text, cellWidth, cellHeight);
		Font font = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1257, true, fontSize);
		return new Phrase(text, font);
	}
	
	private float cellFontSize(String text, float cellWidth, float cellHeight) {
		return determineFontSize(text, cellWidth, cellHeight);
		
	}
	
	private float determineFontSize(String text, float cellWidth, float cellHeight) {
		for (float fontSize = MAX_FONT_SIZE; fontSize >= 5.0; fontSize -= 0.5) {
			int rowNumber = 2;
			if (fontSize <= MAX_FONT_SIZE*2/3 ) {
				rowNumber = 3;
			}
			if (fontSize <= MAX_FONT_SIZE*0.5) {
				rowNumber = 4;
			}
			float minCharsInRow = (float) Math.ceil(text.length()/rowNumber);
			float maxCharWidth = 2.4f;
			float fontRatio = fontSize / MAX_FONT_SIZE;
			float charWidth = maxCharWidth * fontRatio;
			if (minCharsInRow * charWidth < cellWidth) {
				return fontSize;
			}
		}
		return 5.0f;
	}
	
	
	private static class PDFBackground extends PdfPageEventHelper {
		
		private byte[] background;

	    protected PDFBackground(byte[] background) {
			super();
			this.background = background;
		}

		@Override
		public void onEndPage(PdfWriter writer, Document document) {
			try {
				Image image = Image.getInstance(this.background);
				float width = document.getPageSize().getWidth();
		        float height = document.getPageSize().getHeight();
		        writer.getDirectContentUnder().addImage(image, width, 0, 0, height, 0, 0);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	        
	    }
	    
	}
}
