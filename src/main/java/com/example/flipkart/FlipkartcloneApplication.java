package com.example.flipkart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class FlipkartcloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipkartcloneApplication.class, args);
	}

}
