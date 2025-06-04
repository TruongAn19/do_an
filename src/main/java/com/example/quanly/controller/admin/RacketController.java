package com.example.quanly.controller.admin;

import com.example.quanly.domain.Product;
import com.example.quanly.domain.Racket;
import com.example.quanly.repository.ProductRepository;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RacketStockByDateService;
import com.example.quanly.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
public class RacketController {
    @Autowired
    private RacketService racketService;

    @Autowired
    private UploadService uploadService;

    @Autowired
    private  ProductRepository productRepository  ;
    @Autowired
    private RacketStockByDateService racketStockByDateService;

    @GetMapping("/admin/racket")
    public String getByProductPage(Model model,
            @RequestParam("page") Optional<String> optionalPage) {
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
        Page<Racket> byProducts = this.racketService.getAllRacket(pageable);
        List<Racket> listByProducts = byProducts.getContent();
        model.addAttribute("byProducts", listByProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", byProducts.getTotalPages());
        return "admin/racket/by-product";
    }

    @GetMapping("/admin/racket/{racketId}")
    public String getDetailRacket(Model model, @PathVariable long racketId) {
        Optional<Racket> racket = this.racketService.getRacketById(racketId);
        model.addAttribute("racket", racket.get());
        model.addAttribute("id", racketId);
        return "admin/racket/byProduct_detail";
    }

    @GetMapping("/admin/racket/create_racket")
    public String getCreateRacketPage(Model model) {
        List<Product> product = this.productRepository.findAll();
        model.addAttribute("newRacket", new Racket());
        model.addAttribute("productList", product);
        return "admin/racket/create_byProduct";
    }

    @PostMapping(value = "/admin/racket/create")
    public String createProductPage(Model model, @ModelAttribute("newRacket") Racket racket,
            @RequestParam("racketImg") MultipartFile file) {
        String racketImage = this.uploadService.handleSaveUploadFile(file, "racket");
        racket.setImage(racketImage);
        racket.setStatus("AVAILABLE");
        Racket saveRacket = this.racketService.handSaveRacket(racket);
        racketStockByDateService.generateStockForRacket(saveRacket);
        return "redirect:/admin/racket";

    }

    @GetMapping("/admin/racket/update_byProduct/{racketId}")
    public String getUpdateByProductPage(Model model, @PathVariable long racketId) {
        Optional<Racket> existRacket = this.racketService.getRacketById(racketId);
        model.addAttribute("editRacket", existRacket);
        List<Product> product = this.productRepository.findAll();
        model.addAttribute("productList", product);
        return "admin/racket/update_byProduct";
    }

    @PostMapping("/admin/racket/update_racket")
    public String postUpdateProduct(Model model, @ModelAttribute("editRacket") Racket racket,
            @RequestParam("racketImg") MultipartFile file) {
        Optional<Racket> existRacket = this.racketService.getRacketById(racket.getId());

        if (existRacket.isPresent()) {
            Racket existing = existRacket.get();

            existing.setName(racket.getName());
            existing.setPrice(racket.getPrice());
            existing.setFactory(racket.getFactory());
            existing.setAvailable(racket.isAvailable());
            existing.setRentalPricePerDay(racket.getRentalPricePerDay());
            existing.setRentalPricePerPlay(racket.getRentalPricePerPlay());
            existing.setBookingStockQuantity(racket.getBookingStockQuantity());
            existing.setQuantity(racket.getQuantity());
            existing.setStatus(racket.getStatus());
            existing.setProduct(racket.getProduct());// nhớ lấy product
            if (!file.isEmpty()) {
                String racketImg = this.uploadService.handleSaveUploadFile(file, "racket");
                existing.setImage(racketImg);
            }

            this.racketService.handSaveRacket(existing);
        }

        return "redirect:/admin/racket";
    }



}
