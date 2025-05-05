package com.example.quanly.controller.admin;



import com.example.quanly.service.ProductService;
import com.example.quanly.service.RacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.quanly.service.UserService;


@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final UserService userService;
    private final RacketService racketServie;
    private  final ProductService productService;



    @RequestMapping("/admin")
    public String getDashBoard(Model model) {
        model.addAttribute("countUser", this.userService.countUser());
        model.addAttribute("countProduct", this.productService.getCourtProduct());
        model.addAttribute("countByRacket", this.racketServie.countRacket());
        return "admin/dashboard/show";
    }
}
