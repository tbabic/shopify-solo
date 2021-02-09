package org.bytepoet.shopifysolo.manager.controllers;

import org.apache.commons.codec.binary.Base64;
import org.bytepoet.shopifysolo.services.MonthlyOverviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/manager/overview")
public class OverviewController {

	@Autowired
	private MonthlyOverviewService monthlyOverviewService;
	
	
	public class OverviewResponse {

		@JsonProperty
		private String fileName;
		
		@JsonProperty
		private String base64Data;

		public OverviewResponse(String fileName, String base64Data) {
			this.fileName = fileName;
			this.base64Data = base64Data;
		}
		
		
		
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public OverviewResponse overview( @RequestParam("month") int month,  @RequestParam("year") int year) {
		
		String csv = monthlyOverviewService.createMonthlyOverviewFile(month, year);
		String base64Value = Base64.encodeBase64String(csv.getBytes());
		String fileName = "overview-"+month+"-"+year+".csv";
		return new OverviewResponse(fileName, base64Value);
	}
}
