package com.example.flipkart.service;

import org.springframework.http.ResponseEntity;

import com.example.flipkart.requestDto.OtpModel;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.util.ResponseStructure;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> userRegister(UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel);

	

}
