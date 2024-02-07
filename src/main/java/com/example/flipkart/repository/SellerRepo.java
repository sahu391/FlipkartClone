package com.example.flipkart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flipkart.entity.Seller;

public interface SellerRepo extends JpaRepository<Seller, Integer>{

}
