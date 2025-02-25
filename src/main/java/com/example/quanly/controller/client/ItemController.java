package com.example.quanly.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.quanly.domain.Product;
import com.example.quanly.service.ProductService;

@Controller
public class ItemController {
    private final ProductService productService;

    public ItemController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/product/{productId}")
    public String getMethodName(Model model, @PathVariable long productId) {
        Product product = this.productService.geProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "client/product/detail";
    }
    
}
