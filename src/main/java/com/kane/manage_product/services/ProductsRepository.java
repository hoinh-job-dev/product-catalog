package com.kane.manage_product.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kane.manage_product.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

}
