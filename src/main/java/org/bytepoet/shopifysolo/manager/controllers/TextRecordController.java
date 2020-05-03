package org.bytepoet.shopifysolo.manager.controllers;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.TextRecord;
import org.bytepoet.shopifysolo.manager.repositories.TextRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
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
	
	@RequestMapping(path = "/categories", method = RequestMethod.POST)
	@Transactional
	public void updateCategory(@RequestBody Map<String, String> body) {
		String oldCategory = body.get("oldCategory");
		String newCategory = body.get("newCategory");
		if (StringUtils.isBlank(oldCategory)) {
			throw new RuntimeException("New value for category must not be empty"); 
		}
		if (StringUtils.isBlank(newCategory)) {
			throw new RuntimeException("Old value for category must not be empty"); 
		}
		List<TextRecord> records = textRecordRepository.findByCategory(oldCategory);
		records.stream().forEach(record -> record.setCategory(newCategory));
		textRecordRepository.saveAll(records);
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
