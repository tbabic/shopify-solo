package org.bytepoet.shopifysolo.manager.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping("/manager/login")
public class LoginManagerController {

	@RequestMapping(method=RequestMethod.POST)
	public LoginResponse login(@RequestHeader("Authorization") String authorization, Authentication principal) {
		return new LoginResponse(authorization, getRole(principal));
	}
	
	private String getRole(Authentication principal) {
		return principal.getAuthorities().iterator().next().getAuthority();
	}
	
	public static class LoginResponse {
		
		@JsonProperty
		private String authToken;
		
		@JsonProperty
		private String role;

		public LoginResponse(String authToken, String role) {
			super();
			this.authToken = authToken;
			this.role = role;
		}

	
		
	}
}
