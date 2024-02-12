package com.example.flipkart.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class userNotLoggedInException extends RuntimeException {

	private String message;
	
}
