package com.example.flipkart.requestDto;

import com.example.flipkart.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
	
	
	private String email;
	private String password;
	private UserRole userRole;
}
