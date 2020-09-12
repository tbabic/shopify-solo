package org.bytepoet.shopifysolo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UsersConfiguration usersConfiguration;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.httpBasic().and()
			.authorizeRequests()
			.antMatchers("/*").permitAll()
			.antMatchers("/manager/**").authenticated()
			.and()
            .csrf().disable()
            .formLogin().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}
	
	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		List<UserDetails> users = usersConfiguration.getUsernames().stream().map(username -> {
			return User.withDefaultPasswordEncoder()
						.username(username)
						.password(usersConfiguration.getPassword(username))
						.roles(usersConfiguration.getRole(username))
						.build();
		}).collect(Collectors.toList());
		return new InMemoryUserDetailsManager(users);
	}
	

	
}
