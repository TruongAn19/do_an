package com.example.quanly.controller.client;

// import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.quanly.domain.*;
import com.example.quanly.repository.BookingDetailRepository;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.repository.SubCourtRepository;
import com.example.quanly.repository.TimeRepository;
import com.example.quanly.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import com.example.quanly.domain.*;
import com.example.quanly.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ItemController {

    ProductService productService;
    RacketService racketService;


    @GetMapping("/main-products")
    public String getMainProductPage(Model model,
                                     ProductCriteriaDTO productCriteriaDTO,
                                     HttpServletRequest request,
                                     @RequestParam(value = "search", required = false) String searchTerm) {
        int page = 1;
        try {
            if (productCriteriaDTO.getPage().isPresent()) {
                // convert from String to int
                page = Integer.parseInt(productCriteriaDTO.getPage().get());
            } else {
                // page = 1
            }
        } catch (Exception e) {
            // page = 1
            // TODO: handle exception
        }

        // check sort price
        Pageable pageable = PageRequest.of(page - 1, 6);

        Optional<String> sortOpt = productCriteriaDTO.getSort();

        if (sortOpt != null && sortOpt.isPresent()) {
            String sort = sortOpt.get();
            if (sort.equals("gia-tang-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).ascending());
            } else if (sort.equals("gia-giam-dan")) {
                pageable = PageRequest.of(page - 1, 6, Sort.by(Product_.PRICE).descending());
            }
        }

        Page<Product> mainProduct;
        if (searchTerm != null && !searchTerm.isEmpty()) {
            // Chỉ tìm kiếm theo tên nếu có từ khóa
            mainProduct = productService.findByNameContaining(searchTerm, pageable);
        } else if (sortOpt != null && sortOpt.isPresent()
                && (productCriteriaDTO.getAddress() != null || productCriteriaDTO.getPrice() != null)) {
            mainProduct = this.productService.getAllProductWithSpec(pageable, productCriteriaDTO);
        } else {
            mainProduct = this.productService.getAllProduct(pageable);
        }

        int totalPages = Math.max(mainProduct.getTotalPages(), 0);

        String qs = request.getQueryString();
        if (qs != null && !qs.isBlank()) {
            qs = qs.replace("page=" + page, "");
        }

        model.addAttribute("listMainProducts", mainProduct.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("queryString", qs);
        return "client/product/main_product";
    }

    @GetMapping("/by-products")
    public String getByProductPage(Model model,
                                   ProductCriteriaDTO productCriteriaDTO,
                                   HttpServletRequest request) {
        // Lấy thông tin phân trang từ request
        int page = 0;
        int size = 10;
        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 0) page = 0;
        } catch (Exception e) {
            page = 0;
        }
        Pageable pageable = PageRequest.of(page, size);

        // Gọi service để lấy danh sách Racket có phân trang
        Page<Racket> byProduct = racketService.getAllRacket(pageable);

        // Đưa dữ liệu ra model
        model.addAttribute("listByProduct", byProduct.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", byProduct.getTotalPages());

        // QueryString để giữ nguyên filter/search nếu có
        String qs = request.getQueryString();
        model.addAttribute("queryString", qs != null ? qs : "");

        return "client/racket/by_product";
    }


    @GetMapping("/mainProduct/{productId}")
    public String getMainProducts(Model model, @PathVariable long productId) {
        Product product = this.productService.getProductByID(productId);
        List<AvailableTime> availableTime = this.productService.getAllTime();
        double discountPrice = product.getPrice() - (product.getPrice() * product.getSale() / 100);
        model.addAttribute("discountPrice", discountPrice);
        model.addAttribute("availableTime", availableTime); // Gán vào model
        model.addAttribute("product", product);
        model.addAttribute("id", productId);
        return "client/product/detailMainProduct";
    }

    @GetMapping("/racket/{racketId}")
    public String getDetailRacket(Model model, @PathVariable long racketId) {
        Optional<Racket> racket = this.racketService.getRacketById(racketId);
        model.addAttribute("racket", racket.get());
        model.addAttribute("id", racketId);
        return "client/racket/racketDetail";
    }
}
