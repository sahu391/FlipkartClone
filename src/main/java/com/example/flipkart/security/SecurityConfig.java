package com.example.flipkart.security;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
	
	@Autowired
	private CustomUserDetailsService userDetailsService;
	
	
	@Bean
	 PasswordEncoder getBCryptPasswordEncoder() {
		return new BCryptPasswordEncoder(12);
	}
	
	@Bean
	AuthenticationManager authenticationManager (AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}
		
	@Bean
	SecurityFilterChain securityFilterChain (HttpSecurity http) throws Exception {
		return http.csrf(csrf->csrf.disable())
		.authorizeHttpRequests(auth-> auth.requestMatchers("/**").permitAll()
		.anyRequest().authenticated())
		
		.httpBasic(Customizer.withDefaults())
		.build();
	}
	
	@Bean
	AuthenticationProvider authenticationProvider () {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(getBCryptPasswordEncoder());
		return provider;
	}
	
	
}
