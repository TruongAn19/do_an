package com.example.quanly.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.quanly.domain.Product;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.UploadService;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductController {
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService) {
        this.productService = productService;
        this.uploadService = uploadService;
    }

    @RequestMapping("/admin/product")
    public String getProductPage(Model model) {
        List<Product> products = this.productService.getAllProduct();
        model.addAttribute("products", products);
        return "admin/product/show";
    }

    @GetMapping("/admin/product/create")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create";
    }

    @PostMapping(value = "/admin/product/create")
    public String createProductPage(Model model, @ModelAttribute("newProduct") Product product,
            @RequestParam("productImg") MultipartFile file) {
        String productImage = this.uploadService.handleSaveUploadFile(file, "product");
        product.setImage(productImage);
        this.productService.handSaveProduct(product);
        return "redirect:/admin/product";
    }

    @GetMapping("/admin/product/{productId}")
    public String getMethodName(Model model, @PathVariable long productId) {
        Product product = this.productService.geProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "admin/product/product_detail";
    }

    @GetMapping("/admin/product/update_product/{productId}")
    public String getUpdateProductPage(Model model, @PathVariable long productId) {
        Product existProduct = this.productService.geProductByID(productId);
        model.addAttribute("editProduct", existProduct);
        return "admin/product/update_product";
    }

    @PostMapping("/admin/product/update_product")
public String postUpdateProduct(Model model, @ModelAttribute("editProduct") Product product,
                                @RequestParam("productImg") MultipartFile file) {
    Product existProduct = this.productService.geProductByID(product.getId());

    if (existProduct != null) {
        existProduct.setName(product.getName());
        existProduct.setQuantity(product.getQuantity());
        existProduct.setDetailDesc(product.getDetailDesc());
        existProduct.setAddress(product.getAddress());
        existProduct.setSold(product.getSold());
        existProduct.setPrice(product.getPrice());

        // Kiểm tra nếu người dùng có tải lên ảnh mới
        if (!file.isEmpty()) {
            // Lưu ảnh mới và cập nhật đường dẫn
            String productImage = this.uploadService.handleSaveUploadFile(file, "product");
            existProduct.setImage(productImage);
        }
        this.productService.handSaveProduct(existProduct);
    }

    return "redirect:/admin/product";
}


    @GetMapping("/admin/product/delete_product/{productId}")
    public String getDeleteProductPage(Model model, @PathVariable long productId) {
        Product product = this.productService.geProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("productID", productId);
        return "admin/product/delete_product";
    }

    @PostMapping("/admin/product/delete_product")
    public String postMethodName(Model model, @ModelAttribute("product") Product product) {
        this.productService.deleteAPrduct(product.getId());

        return "redirect:/admin/product";
    }

}
