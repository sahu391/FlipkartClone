package com.example.flipkart.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DuplicateEmailOrPassword extends RuntimeException {
	private String message;
}
