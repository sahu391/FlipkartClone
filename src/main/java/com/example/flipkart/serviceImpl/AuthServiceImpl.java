package com.example.flipkart.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.flipkart.Exception.DuplicateEmailOrPassword;
import com.example.flipkart.Exception.OtpInvalid;
import com.example.flipkart.Exception.UnsuccessfulRegistration;
import com.example.flipkart.Exception.UserNameNotFoundException;
import com.example.flipkart.Exception.userNotLoggedInException;
import com.example.flipkart.cache.CacheBeanConfig;
import com.example.flipkart.cache.CacheStore;
import com.example.flipkart.entity.AccessToken;
import com.example.flipkart.entity.Customer;
import com.example.flipkart.entity.RefreshToken;
import com.example.flipkart.entity.Seller;
import com.example.flipkart.entity.User;
import com.example.flipkart.enums.UserRole;
import com.example.flipkart.repository.AccessTokenRepo;
import com.example.flipkart.repository.CustomerRepo;
import com.example.flipkart.repository.RefreshTokenRepo;
import com.example.flipkart.repository.SellerRepo;
import com.example.flipkart.repository.UserRepo;
import com.example.flipkart.requestDto.AuthRequest;
import com.example.flipkart.requestDto.OtpModel;
import com.example.flipkart.requestDto.UserRequest;
import com.example.flipkart.responseDto.AuthResponse;
import com.example.flipkart.responseDto.UserResponse;
import com.example.flipkart.security.JwtService;
import com.example.flipkart.service.AuthService;
import com.example.flipkart.util.CookieManager;
import com.example.flipkart.util.MessageStructure;
import com.example.flipkart.util.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
	
	
	private PasswordEncoder passwordEncoder;
	private CustomerRepo customerRepo;
	private SellerRepo sellerRepo;
	private UserRepo userRepo;
	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authStructure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private RefreshTokenRepo refreshTokenRepo;
	private AccessTokenRepo accessTokenRepo;
	
	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;
	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;
	
	
	



	public AuthServiceImpl(PasswordEncoder passwordEncoder,
			CustomerRepo customerRepo, 
			SellerRepo sellerRepo,
			UserRepo userRepo, 
			ResponseStructure<UserResponse> structure, 
			ResponseStructure<AuthResponse> authStructure,
			CacheStore<String> otpCacheStore, 
			CacheStore<User> userCacheStore, 
			JavaMailSender javaMailSender,
			AuthenticationManager authenticationManager, 
			CookieManager cookieManager, 
			JwtService jwtService,
			RefreshTokenRepo refreshTokenRepo, 
			AccessTokenRepo accessTokenRepo) {
		super();
		this.passwordEncoder = passwordEncoder;
		this.customerRepo = customerRepo;
		this.sellerRepo = sellerRepo;
		this.userRepo = userRepo;
		this.structure = structure;
		this.authStructure = authStructure;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.refreshTokenRepo = refreshTokenRepo;
		this.accessTokenRepo = accessTokenRepo;
	}



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
	
	
	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> userLogin(AuthRequest request,HttpServletResponse response) {
		String username = request.getEmail().split("@")[0];
		String password=request.getPassword();
		
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		Authentication authentication = authenticationManager.authenticate(token);
		if(!authentication.isAuthenticated())
			throw new UserNameNotFoundException("Failed to authenticate the user");
		else 
			return userRepo.findByUserName(username).map(user->{
			grantAcess(response, user);
			return ResponseEntity.ok(authStructure.setStatus(HttpStatus.OK.value())
					.setData(AuthResponse.builder()
							.userId(user.getUserId())
							.userName(username)
							.role(user.getUserRole().name())
							.isAuthenticated(true)
							.accessExpirationInSeconds(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
							.refreshExpirationInSeconds(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
							.build())
					.setMessage("Login successful"));
			}).get();
		
	}

	
	
	@Override
	public ResponseEntity<String> userLogout(String refreshToken, String accessToken, HttpServletResponse response) {
		if(accessToken==null && refreshToken ==null) throw new userNotLoggedInException("The user needs to login !");
		
		accessTokenRepo.findByToken(accessToken).ifPresent(accesstoken->{
			accesstoken.setBlocked(true);
			accessTokenRepo.save(accesstoken);
		});
		
		refreshTokenRepo.findByToken(refreshToken).ifPresent(refreshtoken->{
			refreshtoken.setBlocked(true);
			refreshTokenRepo.save(refreshtoken);
		});
		
		response.addCookie(cookieManager.invalidate(new Cookie("acessToken", "")));
		response.addCookie(cookieManager.invalidate(new Cookie("refreshToken", "")));
		
		return ResponseEntity.ok("Logged out successfully");
	}

//=====================================================================================================================================
	
	
	
	
	private void grantAcess(HttpServletResponse response,User user) {
		
		//generating access and refresh tokens 
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());
		
		//adding access and refresh token cookie to the response
		response.addCookie(cookieManager.configure(new Cookie("at", accessToken),accessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken),refreshExpiryInSeconds));
		
		//saving the access and response cookie into the database
		accessTokenRepo.save(AccessToken.builder()
				.user(user)
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());
		
		refreshTokenRepo.save(RefreshToken.builder()
				.user(user)
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());
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
