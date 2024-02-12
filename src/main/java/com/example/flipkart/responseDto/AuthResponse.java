package com.example.flipkart.responseDto;

import java.time.LocalDateTime;

import com.example.flipkart.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
	private int userId;
	private String userName;
	private String role;
	private boolean isAuthenticated;
	private LocalDateTime  accessExpirationInSeconds;
	private LocalDateTime  refreshExpirationInSeconds;
}
