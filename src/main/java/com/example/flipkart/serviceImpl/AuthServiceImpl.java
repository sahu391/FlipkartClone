package com.example.flipkart.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.flipkart.Exception.DuplicateEmailOrPassword;
import com.example.flipkart.entity.Customer;
import com.example.flipkart.entity.Seller;
import com.example.flipkart.entity.User;
import com.example.flipkart.enums.UserRole;
import com.example.flipkart.repository.CustomerRepo;
import com.example.flipkart.repository.SellerRepo;
import com.example.flipkart.repository.UserRepo;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.service.AuthService;
import com.example.flipkart.util.ResponseStructure;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	
	
	private PasswordEncoder passwordEncoder;
	private CustomerRepo customerRepo;
	private SellerRepo sellerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	
	
	
	private <T extends User>T mapToUser(UserRequest userRequest) {
		User user=null;
		switch(userRequest.getUserRole()){
			case CUSTOMER:{user=new Customer();}
			break;
			case SELLER:{user=new Seller();}
			break;
			}
         user.setEmail(userRequest.getEmail());
         user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
         user.setUserRole(userRequest.getUserRole());
         user.setUserName(userRequest.getEmail().split("@")[0]);
         user.setEmailVerified(false);
         user.setDeleted(false);
		return (T)user;		
		
	}
	
	private  UserResponse mapToUserResponse(User user) {
		return  UserResponse.builder()
				.userId(user.getUserId())
				.userName(user.getUserName())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();
				
	}

	private <T extends User>T saveUser(UserRequest userRequest) {
			
		User user=null;
		switch(userRequest.getUserRole()){
			case CUSTOMER:{user=customerRepo.save(mapToUser(userRequest));}
			break;
			case SELLER:{user=sellerRepo.save(mapToUser(userRequest));}
			break;
			}
		return (T)user;	
	}
	
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> userRegister(UserRequest userRequest) {
		User user=userRepo.findByUserName(userRequest.getEmail().split("@")[0])
				          .map(u->{
			if(u.isEmailVerified()) 
				throw new RuntimeException("user already exists by specified by emailId");
			else {
				//send an email to user with an OTP
			}
			return u;
		}).orElseGet(saveUser(userRequest));
		
		
			structure.setStatus(HttpStatus.ACCEPTED.value());
			structure.setMessage("User Registered Successfully");
		    structure.setData(mapToUserResponse(user));
		    
		    
			return new ResponseEntity<ResponseStructure<UserResponse>> (structure, HttpStatus.ACCEPTED);
			
			
		
		
		
	}

	

	

	



	
}
