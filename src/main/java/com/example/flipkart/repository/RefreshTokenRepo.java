package com.example.flipkart.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.AccessToken;
import com.example.flipkart.entity.RefreshToken;
import com.example.flipkart.entity.User;
import com.example.flipkart.requestDto.UserRequest;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
	
	Optional<RefreshToken> findByToken(String token);
	List<RefreshToken> findAllByExpirationBeforeAndIsBlocked (LocalDateTime expiration,boolean isBlocked);

	List<RefreshToken> findByUserAndIsBlocked(User user, boolean isBlocked);

	List<RefreshToken> findByUserAndIsBlockedAndTokenNot(User user, boolean b,
			String token);
	
	
	
}
