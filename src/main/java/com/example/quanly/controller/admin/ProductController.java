package com.example.quanly.controller.admin;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.example.quanly.service.BookingStatsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProductController {
    
    ProductService productService;
    UploadService uploadService;
    BookingStatsService bookingStatsService;

    @GetMapping("/admin/mainProduct")
    public String getMainProductPage(Model model,
            @RequestParam("page") Optional<String> optionalPage,
            @RequestParam(value = "search", required = false) String searchTerm) {
        int page = 1;
        try {
            if (optionalPage.isPresent()) {
                page = Integer.parseInt(optionalPage.get());
            } else {
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        Pageable pageable = PageRequest.of(page - 1, 4);
        Page<Product> mainProducts;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Tìm kiếm sản phẩm theo tên
            mainProducts = this.productService.findByNameContaining(searchTerm, pageable);
            model.addAttribute("searchTerm", searchTerm); // Giữ lại từ khóa tìm kiếm
        } else {
            // Lấy tất cả sản phẩm
            mainProducts = this.productService.getAllProduct(pageable);
        }
        model.addAttribute("mainProducts", mainProducts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", mainProducts.getTotalPages());
        return "admin/product/main-product";
    }

    @GetMapping("/admin/product/create_mainProduct")
    public String getCreateProductPage(Model model) {
        model.addAttribute("newProduct", new Product());
        return "admin/product/create_mainProduct";
    }

    @PostMapping(value = "/admin/product/create")
    public String createProductPage(Model model, @ModelAttribute("newProduct") Product product,
            @RequestParam("productImg") MultipartFile file, @RequestParam("productType") String productType) {
        String productImage = this.uploadService.handleSaveUploadFile(file, "product");
        product.setDetailDesc(product.getDetailDesc().replace("\n", "<br>"));
        product.setImage(productImage);
        this.productService.handSaveProduct(product);

        return "redirect:/admin/mainProduct";

    }

    @GetMapping("/admin/mainProduct/{productId}")
    public String getMainProduct(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "admin/product/mainProduct_detail";
    }

    @GetMapping("/admin/product/update_mainProduct/{productId}")
    public String getUpdateMainProductPage(Model model, @PathVariable long productId) {
        Product existProduct = this.productService.getProductByID(productId);
        model.addAttribute("editProduct", existProduct);
        return "admin/product/update_mainProduct";
    }

    @PostMapping("/admin/product/update_product")
    public String postUpdateProduct(Model model, @ModelAttribute("editProduct") Product product,
            @RequestParam("productImg") MultipartFile file) {
        Product existProduct = this.productService.getProductByID(product.getId());

        if (existProduct != null) {
            existProduct.setName(product.getName());
            existProduct.setDetailDesc(product.getDetailDesc());
            existProduct.setAddress(product.getAddress());
            existProduct.setSale(product.getSale());
            existProduct.setPrice(product.getPrice());

            // Kiểm tra nếu người dùng có tải lên ảnh mới
            if (!file.isEmpty()) {
                // Lưu ảnh mới và cập nhật đường dẫn
                String productImage = this.uploadService.handleSaveUploadFile(file, "product");
                existProduct.setImage(productImage);
            }
            this.productService.handSaveProduct(existProduct);
        }
        return "redirect:/admin/mainProduct";

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

        return "redirect:/admin/mainProduct";
    }

    @GetMapping("/admin/statistics/revenue")
    public String revenueForm() {
        return "admin/chart/revenue_chart";
    }

    @PostMapping("/admin/statistics/revenue")
    public String revenueChart(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        // Chuyển LocalDate thành java.util.Date
        Date startDateConverted = java.util.Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDateConverted = java.util.Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Lấy dữ liệu doanh thu
        Map<String, Double> revenueData = bookingStatsService.getRevenueBetweenDates(startDate, endDate);

        // Đưa vào model
        model.addAttribute("revenueData", revenueData);
        model.addAttribute("startDate", startDateConverted);
        model.addAttribute("endDate", endDateConverted);

        return "admin/chart/revenue_chart";
    }

}
