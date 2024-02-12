package com.example.flipkart.service;

import org.springframework.http.ResponseEntity;

import com.example.flipkart.requestDto.AuthRequest;
import com.example.flipkart.requestDto.OtpModel;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.AuthResponse;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.util.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> userRegister(UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel);

	ResponseEntity<ResponseStructure<AuthResponse>> userLogin(AuthRequest request, HttpServletResponse response);

	ResponseEntity<String> userLogout(String refreshToken, String accessToken, HttpServletResponse response);

	

}
