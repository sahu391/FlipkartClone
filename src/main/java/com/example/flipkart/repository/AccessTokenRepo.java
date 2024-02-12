package com.example.flipkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.AccessToken;
import com.example.flipkart.entity.User;
import com.example.flipkart.requestDto.UserRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{
		
	Optional<AccessToken> findByToken(String token);
	List<AccessToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);
	Optional<AccessToken> findByTokenAndIsBlocked(String at,boolean b);
	
	List<AccessToken> findByUserAndAccessTokenIsBlocked(User user, boolean isBlocked);

	List<AccessToken> findByUserAndAccessTokenIsBlockedAndAccessTokenNot(User user, boolean isBlocked, String accessToken);
	
}
