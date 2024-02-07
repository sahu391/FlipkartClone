package com.example.flipkart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.service.AuthService;
import com.example.flipkart.util.ResponseStructure;

import lombok.AllArgsConstructor;

@RestController
//@RequestMapping("/api/v1")
@AllArgsConstructor
public class AuthController {
	
	
	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>>  userRegister(@RequestBody UserRequest userRequest) {
		return authService.userRegister(userRequest);
	}
	
	
}
