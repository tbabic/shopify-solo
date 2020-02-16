package org.bytepoet.shopifysolo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "users")
@Configuration
public class UsersConfiguration {


	private Map<String, String> passwords;
	private Map<String, String> roles;
	
	public Map<String, String> getPasswords() {
		return passwords;
	}
	
	public void setPasswords(Map<String, String> passwords) {
		this.passwords = passwords;
	}

	public Map<String, String> getRoles() {
		return roles;
	}

	public void setRoles(Map<String, String> roles) {
		this.roles = roles;
	}

	public String getPassword(String username) {
		return passwords.get(username);
	}
	
	public String getRole(String username) {
		return roles.getOrDefault(username, "USER");
	}
	
	

	public Collection<String> getUsernames() {
		if (passwords == null) {
			return Collections.emptyList();
		}
		return passwords.keySet();
	}
}
