package com.example.quanly.controller.client;

// import static org.mockito.Mockito.times;

import com.example.quanly.domain.AvailableTime;
import com.example.quanly.domain.Product;
import com.example.quanly.domain.Product_;
import com.example.quanly.domain.Racket;
import com.example.quanly.domain.dto.ProductCriteriaDTO;
import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
            mainProduct = this.productService.getAllProductClient(pageable);
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
    public String getByProductPage(
            @RequestParam(value = "factory", required = false) String[] factories,
            @RequestParam(value = "price", required = false) String[] prices,
            @RequestParam(value = "sort", required = false, defaultValue = "gia-nothing") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        int size = 6;
        Pageable pageable = PageRequest.of(page, size);

        // Chuyển mảng thành List
        List<String> factoryList = (factories != null && factories.length > 0) ? Arrays.asList(factories) : null;
        List<String> priceList = (prices != null && prices.length > 0) ? Arrays.asList(prices) : null;

        // Gọi service lọc
        Page<Racket> racketPage = racketService.getRackets(factoryList, priceList, sort, pageable);

        // Truyền dữ liệu sang view
        model.addAttribute("listRacket", racketPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", racketPage.getTotalPages());
        model.addAttribute("totalElements", racketPage.getTotalElements());

        // Giữ lại lựa chọn người dùng
        model.addAttribute("selectedFactories", factoryList);
        model.addAttribute("selectedPrices", priceList);
        model.addAttribute("selectedSort", sort);

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
