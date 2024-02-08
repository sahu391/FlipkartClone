package com.example.flipkart.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.flipkart.Exception.DuplicateEmailOrPassword;
import com.example.flipkart.Exception.OtpInvalid;
import com.example.flipkart.Exception.UnsuccessfulRegistration;
import com.example.flipkart.cache.CacheBeanConfig;
import com.example.flipkart.cache.CacheStore;
import com.example.flipkart.entity.Customer;
import com.example.flipkart.entity.Seller;
import com.example.flipkart.entity.User;
import com.example.flipkart.enums.UserRole;
import com.example.flipkart.repository.CustomerRepo;
import com.example.flipkart.repository.SellerRepo;
import com.example.flipkart.repository.UserRepo;
import com.example.flipkart.requestDto.OtpModel;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.service.AuthService;
import com.example.flipkart.util.MessageStructure;
import com.example.flipkart.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
	
	
	private PasswordEncoder passwordEncoder;
	private CustomerRepo customerRepo;
	private SellerRepo sellerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;
	
	
	
	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> userRegister(UserRequest userRequest) {
		if(userRepo.existsByEmail(userRequest.getEmail()))
			throw new DuplicateEmailOrPassword("User Email or password exists already!");
		
		String otp=generateOtp();
		
		
		User user=mapToUser(userRequest);
		
        userCacheStore.add(userRequest.getEmail(), user);
		otpCacheStore.add(userRequest.getEmail(),otp);
		
		try {
			sendOtpToMail(user, otp);
		   }
		catch(MessagingException e) {
			log.error("The mail address doesn't exist!");
		}
		
		
		
			structure.setStatus(HttpStatus.ACCEPTED.value());
			structure.setMessage("please verify through OTP sent to your emailId");
		    structure.setData(mapToUserResponse(user));
		    
		    
			return new ResponseEntity<ResponseStructure<UserResponse>> (structure, HttpStatus.ACCEPTED);
			
			
	}

	

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel) {
		User user=userCacheStore.get(otpModel.getEmail());
		String otp=otpCacheStore.get(otpModel.getEmail());
		
		if(otp == null) throw new OtpInvalid("otp expired");
		if(user == null) throw new UnsuccessfulRegistration("Registration session expired");
		if(!otp.equals(otpModel.getOtp())) throw new OtpInvalid("invalid OTP");
		
		user.setEmailVerified(true);
		userRepo.save(user);
		try {
			confirmationMailOfRegistration(user);
		   }
		catch(MessagingException e) {
			log.error("Registration Unsuccessful!");
		}
		return new  ResponseEntity<ResponseStructure<UserResponse>> (structure.setStatus(HttpStatus.CREATED.value())
				.setMessage("registration successful")
				.setData(mapToUserResponse(user)),HttpStatus.CREATED);
		
	}
	
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
	
	
	private String generateOtp() {
	 return String.valueOf(new Random().nextInt(100000,999999));
    }
	
	private void sendOtpToMail(User user,String otp) throws MessagingException {
		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Complete your Registration to Flipkart")
		.sentDate(new Date())
		.text(
				"hey,"+user.getUserName()
				+"Good to see you interested in Flipkart"
				+"complete your registration using OTP <br>"
				+"<h1>"+otp+"<h1><br>"
				+"Note: The OTP expires in 1 minute"
				+"<br><br>"
				+"with best  regards<br>"
				+"Flipkart"
			).build());
	}
	
	private void confirmationMailOfRegistration(User user) throws MessagingException {
		sendMail(MessageStructure.builder()
		.to(user.getEmail())
		.subject("Successfully Registered to flipkart")
		.sentDate(new Date())
		.text(
				"hey,"+user.getUserName()
				+"Good to see you interested in Flipkart"
				+"Your Registration is successful,you can explore"
				+"<br><br>"
				+"with best  regards<br>"
				+"Flipkart"
			).build());
	}
	
	
	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mailMessage=javaMailSender.createMimeMessage();
		MimeMessageHelper helper= new MimeMessageHelper(mailMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(),true);
		javaMailSender.send(mailMessage);
		
		
	}

	
	public void deleteIfNotVerified() {

		List<User> users =userRepo.findByIsEmailVerified(false);
		userRepo.deleteAll(users);
		
	}


	
}
