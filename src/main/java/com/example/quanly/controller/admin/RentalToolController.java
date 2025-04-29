package com.example.quanly.controller.admin;


import com.example.quanly.domain.Racket;
import com.example.quanly.domain.RentalTool;
import com.example.quanly.repository.RentalToolRepository;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RentalToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class RentalToolController {

    @Autowired
    private RentalToolService rentalToolService;
    @Autowired
    private RacketService racketService;
    @Autowired
    private RentalToolRepository rentalToolRepository;

    // Danh sách vợt thuê loại DAILY
    @GetMapping("/admin/rental")
    public String getListRental(Model model) {
        List<RentalTool> rentals = this.rentalToolService.getRentalByTypeDAILY();
        model.addAttribute("rentals", rentals);
        return "admin/rental_manager/show";  // Chuyển đến trang show.jsp
    }

    // Chi tiết vợt thuê
    // Cập nhật vợt thuê
// Chi tiết vợt thuê
    @GetMapping("/admin/rental/{id}")
    public String getRentalDetail(@PathVariable Long id, Model model) {
        RentalTool rentalTool = rentalToolService.getRentalToolById(id);
        // Lấy thông tin vợt từ bảng racket
        Racket racket = racketService.getRacketById(Long.parseLong(rentalTool.getRacketId())).get();

        // Thêm thông tin vợt vào mô hình (model)
        model.addAttribute("rentalTool", rentalTool);
        model.addAttribute("racket", racket);

        return "admin/rental_manager/detail";  // Chuyển đến trang detail.jsp
    }


    // Mở form cập nhật thông tin vợt thuê
    // Cập nhật vợt thuê
    @GetMapping("/admin/rental/update/{id}")
    public String showUpdateRentalToolForm(@PathVariable Long id, Model model) {
        RentalTool rentalTool = rentalToolService.getRentalToolById(id);
        // Lấy thông tin vợt từ bảng racket
        Racket racket = racketService.getRacketById(Long.parseLong(rentalTool.getRacketId())).get();

        // Thêm thông tin vợt vào mô hình (model) cho form cập nhật
        model.addAttribute("rentalTool", rentalTool);
        model.addAttribute("racket", racket);

        return "admin/rental_manager/update";  // Chuyển đến trang update.jsp
    }


    // Cập nhật thông tin vợt thuê
    @PostMapping("/admin/rental/update/{id}")
    public String updateRental(@PathVariable Long id, @ModelAttribute RentalTool rentalTool, Model model) {
        RentalTool updatedRental = rentalToolService.changeStatus(id, rentalTool.getStatus());
        model.addAttribute("rentalTool", updatedRental);
        return "redirect:/admin/rental";  // Quay lại danh sách
    }
}

