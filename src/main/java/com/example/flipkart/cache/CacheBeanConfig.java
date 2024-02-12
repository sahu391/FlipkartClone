package com.example.flipkart.cache;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.flipkart.entity.User;

@Configuration
public class CacheBeanConfig {
	
	@Bean
	public CacheStore<User> userCacheStore(){
		return new CacheStore<User>(Duration.ofSeconds(60));
	}
	
	@Bean
	public CacheStore<String> otpCacheStore(){
		return new CacheStore<String>(Duration.ofSeconds(60));
	}
}
