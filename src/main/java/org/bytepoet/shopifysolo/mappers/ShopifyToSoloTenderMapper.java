package org.bytepoet.shopifysolo.mappers;

import org.bytepoet.shopifysolo.shopify.models.ShopifyOrder;
import org.bytepoet.shopifysolo.solo.models.SoloTender;
import org.bytepoet.shopifysolo.solo.models.SoloTender.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ShopifyToSoloTenderMapper extends ShopifyToSoloMapper<SoloTender, SoloTender.Builder>{


	@Value("${soloapi.tender_note_format}")
	private String tenderNoteFormat;
	
	@Value("${soloapi.note}")
	private String note;
	
	@Override
	protected Builder getBuilder() {
		return new SoloTender.Builder();
	}

	@Override
	protected void additionalMappings(ShopifyOrder order, Builder builder) {
	}
	
}
