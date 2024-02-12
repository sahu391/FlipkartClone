package com.example.flipkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.AccessToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface AccessTokenRepo extends JpaRepository<AccessToken, Long>{
		
	Optional<AccessToken> findByToken(String token);
	List<AccessToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);
	Optional<AccessToken> findByTokenAndIsBlocked(String at,boolean b);
	
}
