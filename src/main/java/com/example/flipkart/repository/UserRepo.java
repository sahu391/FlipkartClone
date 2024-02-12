package com.example.flipkart.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.User;

public interface UserRepo extends JpaRepository<User, Integer>{

	Optional<User> findByUserName(String username);
	List<User> findByIsEmailVerified(boolean isEmailVerified);
	boolean existsByEmail(String email);
	
}
