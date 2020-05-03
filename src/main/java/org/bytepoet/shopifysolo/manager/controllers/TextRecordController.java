package org.bytepoet.shopifysolo.manager.controllers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.TextRecord;
import org.bytepoet.shopifysolo.manager.repositories.TextRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/manager/texts")
@RestController
public class TextRecordController {
	
	@Autowired
	private TextRecordRepository textRecordRepository;

	@RequestMapping(path = "/categories", method = RequestMethod.GET)
	public List<String> getCategories() {
		return textRecordRepository.getAllCategories(); 
	}
	
	@RequestMapping(path = "/records", method = RequestMethod.GET)
	public List<TextRecord> getTextRecords(@RequestParam(name="category", required=false) String category) {
		if(StringUtils.isBlank(category)) {
			return textRecordRepository.findAll();
		} else {
			return textRecordRepository.findByCategory(category);
		}
	}
	
	@RequestMapping(path = "/records", method = RequestMethod.POST)
	public TextRecord saveTextRecord(@RequestBody TextRecord textRecord) {
		if (StringUtils.isBlank(textRecord.getCategory())) {
			throw new RuntimeException("Record must have a category"); 
		}
		if (StringUtils.isBlank(textRecord.getValue())) {
			throw new RuntimeException("Record must have a text value populated"); 
		}
		return textRecordRepository.save(textRecord);
	}
}
