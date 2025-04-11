package com.example.quanly.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.quanly.domain.Product;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.UploadService;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ProductController {

    // private final DashboardController dashboardController;
    private final ProductService productService;
    private final UploadService uploadService;

    public ProductController(ProductService productService, UploadService uploadService,
            DashboardController dashboardController) {
        this.productService = productService;
        this.uploadService = uploadService;
        // this.dashboardController = dashboardController;
    }

    @GetMapping("/admin/mainProduct")
    public String getMainProductPage(Model model,
    @RequestParam("page") Optional<String> optionalPage) {
        int page = 1;
        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Product> mainProducts = this.productService.getAllMainProduct(pageable);
        List<Product> listMainProducts = mainProducts.getContent();
        model.addAttribute("mainProducts", listMainProducts);
        return "admin/product/main-product";
    }

    @GetMapping("/admin/by-product")
    public String getByProductPage(Model model,
            @RequestParam("page") Optional<String> optionalPage) {
        int page = 1;
        try {
            if (optionalPage.isPresent()) {
                page = Integer.parseInt(optionalPage.get());
            } else {}
        } catch (Exception e) {
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Product> byProducts = this.productService.getAllByProduct(pageable);
        List<Product> listByProducts = byProducts.getContent();
        model.addAttribute("byProducts", listByProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", byProducts.getTotalPages());
        return "admin/product/by-product";
    }

    @GetMapping("/admin/product/create_mainProduct")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create_mainProduct";
    }

    @GetMapping("/admin/product/create_byProduct")
    public String getCreateByProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create_byProduct";
    }

    @PostMapping(value = "/admin/product/create")
    public String createProductPage(Model model, @ModelAttribute("newProduct") Product product,
            @RequestParam("productImg") MultipartFile file, @RequestParam("productType") String productType ) {
        String productImage = this.uploadService.handleSaveUploadFile(file, "product");
        product.setDetailDesc(product.getDetailDesc().replace("\n", "<br>"));
        product.setImage(productImage);
        this.productService.handSaveProduct(product);
        if ("byProduct".equals(productType)) {
            return "redirect:/admin/by-product";
        } else {
            return "redirect:/admin/mainProduct";
        }
    }

    @GetMapping("/admin/mainProduct/{productId}")
    public String getMainProduct(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "admin/product/mainProduct_detail";
    }

    @GetMapping("/admin/byProduct/{productId}")
    public String getMethodName(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "admin/product/byProduct_detail";
    }

    @GetMapping("/admin/product/update_mainProduct/{productId}")
    public String getUpdateMainProductPage(Model model, @PathVariable long productId) {
        Product existProduct = this.productService.getProductByID(productId);
        model.addAttribute("editProduct", existProduct);
        return "admin/product/update_mainProduct";
    }

    @GetMapping("/admin/product/update_byProduct/{productId}")
    public String getUpdateByProductPage(Model model, @PathVariable long productId) {
        Product existProduct = this.productService.getProductByID(productId);
        model.addAttribute("editProduct", existProduct);
        return "admin/product/update_byProduct";
    }

    @PostMapping("/admin/product/update_product")
    public String postUpdateProduct(Model model, @ModelAttribute("editProduct") Product product,
            @RequestParam("productImg") MultipartFile file, @RequestParam("productType") String productType) {
        Product existProduct = this.productService.getProductByID(product.getId());

        if (existProduct != null) {
            existProduct.setName(product.getName());
            existProduct.setQuantity(product.getQuantity());
            existProduct.setDetailDesc(product.getDetailDesc().replace("\n", "<br>"));
            existProduct.setAddress(product.getAddress());
            existProduct.setSale(product.getSale());
            existProduct.setPrice(product.getPrice());
            existProduct.setFactory(product.getFactory());


            // Kiểm tra nếu người dùng có tải lên ảnh mới
            if (!file.isEmpty()) {
                // Lưu ảnh mới và cập nhật đường dẫn
                String productImage = this.uploadService.handleSaveUploadFile(file, "product");
                existProduct.setImage(productImage);
            }
            this.productService.handSaveProduct(existProduct);
        }

        if ("byProduct".equals(productType)) {
            return "redirect:/admin/by-product";
        } else {
            return "redirect:/admin/mainProduct";
        }
    }

    @GetMapping("/admin/product/delete_product/{productId}")
    public String getDeleteProductPage(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("productID", productId);
        return "admin/product/delete_product";
    }

    @PostMapping("/admin/product/delete_product")
    public String postMethodName(Model model, @ModelAttribute("product") Product product) {
        this.productService.deleteAllProduct(product.getId());

        return "redirect:/admin/product";
    }

}
