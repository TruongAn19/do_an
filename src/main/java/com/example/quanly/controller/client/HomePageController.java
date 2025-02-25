package com.example.quanly.controller.client;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.quanly.domain.Product;
import com.example.quanly.service.ProductService;



@Controller
public class HomePageController {
    private final ProductService productService;

    public HomePageController (ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/HomePage")
    public String getHomePage(Model model) {
        List<Product> products = this.productService.getAllProduct();
        model.addAttribute("products", products);
        return "client/homepage/show";
    }
    
    
}
