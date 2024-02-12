package com.example.flipkart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flipkart.requestDto.AuthRequest;
import com.example.flipkart.requestDto.OtpModel;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.AuthResponse;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.service.AuthService;
import com.example.flipkart.util.ResponseStructure;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthController {
	
	
	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>>  userRegister(@RequestBody UserRequest userRequest) {
		return authService.userRegister(userRequest);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody OtpModel otpModel){
		return authService.verifyOTP(otpModel);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> userLogin(@RequestBody AuthRequest request,HttpServletResponse response){
		return authService.userLogin(request,response);
	}
	
	@PostMapping("/userlogout")
	public ResponseEntity<String> userLogout(@CookieValue(name="rt",required = false) String refreshToken, 
			@CookieValue(name="at",required = false) String accessToken,HttpServletResponse response){
		return authService.userLogout(refreshToken,accessToken,response);
	}
	
	
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	@PutMapping("/revoke-all")
	public ResponseEntity<String> revokeAllDevices(@CookieValue(name = "at", required = false) String accessToken,
			@CookieValue(name = "rt", required = false) String refreshToken,HttpServletResponse response){
		return authService.revokeAllDevices(accessToken, refreshToken,response);
	}

	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	@PutMapping("/revoke-other")
	public ResponseEntity<String> revokeAllOtherDevices(@CookieValue(name = "at", required = false) String accessToken,
			@CookieValue(name = "rt", required = false) String refreshToken,HttpServletResponse response){
		return authService.revokeAllOtherDevices(accessToken, refreshToken,response);
	}
}
