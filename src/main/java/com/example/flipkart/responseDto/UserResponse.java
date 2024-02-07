package com.example.flipkart.responseDto;

import com.example.flipkart.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
	
	private int userId;
	private String userName;
	private String email;
	private UserRole userRole;
}
