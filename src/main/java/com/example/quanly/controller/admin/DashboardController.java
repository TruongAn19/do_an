package com.example.quanly.controller.admin;



import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.quanly.service.UserService;


@Controller
public class DashboardController {
    private final UserService userService;


    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping("/admin")
    public String getDashBoard(Model model) {
        model.addAttribute("countUser", this.userService.countUser());
        // model.addAttribute("countByProduct", this.userService.countByProduct());
        model.addAttribute("countMainProduct", this.userService.countMainProduct());
        return "admin/dashboard/show";
    }
}
