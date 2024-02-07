package com.example.flipkart.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.flipkart.serviceImpl.AuthServiceImpl;

@Component
public class ScheduledJobs {
	
	@Autowired
	private AuthServiceImpl authservice;
	
	@Scheduled(fixedDelay = 1000L*60*60*24)
	public void test() {
		authservice.deleteIfNotVerified();
	}
}
