package org.bytepoet.shopifysolo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path= {"/","/health"})
@RestController
public class HealthCheckController {

	@GetMapping
	public String healthCheck() {
		return "ok";
	}
}
