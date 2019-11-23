package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.manager.models.PaymentOrder;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.bytepoet.shopifysolo.solo.models.SoloTender.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderToSoloTenderMapper extends OrderToSoloMapper<SoloTender, SoloTender.Builder>{


	@Value("${soloapi.tender_note_format}")
	private String tenderNoteFormat;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Override
	protected Builder getBuilder() {
		return new SoloTender.Builder();
	}

	@Override
	protected void additionalMappings(PaymentOrder order, Builder builder) {
		
	}
	
}
