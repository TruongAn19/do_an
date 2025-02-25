package com.example.quanly.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.quanly.domain.Product;
import com.example.quanly.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProduct() {
        return this.productRepository.findAll();
    }
    
    public Product handSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Product geProductByID(long productId) {
        return this.productRepository.findById(productId);
    }

    public void deleteAPrduct(long productId) {
        this.productRepository.deleteById(productId);
    }
}
