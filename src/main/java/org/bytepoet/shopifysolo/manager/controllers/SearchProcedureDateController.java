package org.bytepoet.shopifysolo.manager.controllers;

import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.SearchProcedureDate;
import org.bytepoet.shopifysolo.manager.repositories.SearchProcedureDateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/search-procedure-date")
public class SearchProcedureDateController {
	
	@Autowired
	private SearchProcedureDateRepository repository;
	
	@RequestMapping("/last")
	public SearchProcedureDate getLast() {
		return repository.findTopByOrderByDateDesc();
	}
	
	@RequestMapping(path="/{id}", method=RequestMethod.GET)
	public SearchProcedureDate get(@PathVariable("id") UUID id) {
		return repository.findById(id).get();
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public SearchProcedureDate save(@RequestBody SearchProcedureDate body) {
		return repository.save(body);
	}
}
