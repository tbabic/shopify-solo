package org.bytepoet.shopifysolo.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;


@Component
public class PdfSearchRefundService {

	@Autowired
	private EncryptionService encryptionService;
	
	public byte[] generateRefundRequest(Order order) {
		
		
		
		
		try (InputStream input = decryptResource(new ClassPathResource("refund-request.dat"));
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();	) {
			PdfReader reader = new PdfReader(input);
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdfDoc = new PdfDocument(reader, writer);
			
			
			PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);
			
			Map<String, PdfFormField> fields = form.getFormFields();
			fields.get("Veza broj pošiljkezapisnikprigovorRow1").setValue(order.getTrackingNumber());
			double price = 0;
			if (order instanceof PaymentOrder) {
				price =  ((PaymentOrder)order).getTotalPrice();
				
			}
			fields.get("Iznos knRow1").setValue(getDecimalFormat().format(price));
			String date = new SimpleDateFormat("dd.MM.yyyy").format(new Date());
			fields.get("Datum podnošenja zahtjevaRow1").setValue(date);
			
			PdfFormField signatureField = fields.get("Potpis podnositelja zahtjevaRow1");
			
			Document document = new Document(pdfDoc, pdfDoc.getDefaultPageSize(), false);
			
			
			Image signature = createSignature();
			signature.setFixedPosition(430, 150);
			signature.scale(0.1f, 0.1f);
			
			form.flattenFields();
			document.add(signature);

			document.close();

			return outputStream.toByteArray();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private Image createSignature() {
		try(InputStream input = new ClassPathResource("signature.dat").getInputStream();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();){
			encryptionService.decrypt(input, outputStream);
			ImageData data = ImageDataFactory.create(outputStream.toByteArray());
			Image image = new Image(data);
			image.setAutoScale(false);
			image.scale(0.5f, 0.5f);
			return image;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private DecimalFormat getDecimalFormat() {
		DecimalFormat df = new DecimalFormat("0.00");
		df.setRoundingMode(RoundingMode.HALF_UP);
		DecimalFormatSymbols newSymbols = new DecimalFormatSymbols();
		newSymbols.setDecimalSeparator(',');
		df.setDecimalFormatSymbols(newSymbols);
		return df;
	}
	
	
	private InputStream decryptResource(ClassPathResource resource) {
		try (InputStream input = resource.getInputStream();
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();	) {
			
			encryptionService.decrypt(input, outputStream);
			
			return new ByteArrayInputStream(outputStream.toByteArray());
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void encryptResources() {
		try (InputStream input = new ClassPathResource("Zahtjev_za_naknadu_stete_i_povrat_postarine_520.pdf").getInputStream();
				FileOutputStream outputStream = new FileOutputStream("D:\\refund-request.dat");	) {
			
			encryptionService.encrypt(input, outputStream);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		try (InputStream input = new ClassPathResource("signature.JPG").getInputStream();
				FileOutputStream outputStream = new FileOutputStream("D:\\signature.dat");	) {
			
			encryptionService.encrypt(input, outputStream);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
