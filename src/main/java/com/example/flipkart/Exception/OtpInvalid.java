package com.example.flipkart.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OtpInvalid extends RuntimeException {
	private String message;
	
}
