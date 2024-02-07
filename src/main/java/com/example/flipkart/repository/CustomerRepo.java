package com.example.flipkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.Customer;

public interface CustomerRepo extends JpaRepository<Customer, Integer>{

}
