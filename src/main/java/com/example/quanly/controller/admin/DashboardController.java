package com.example.quanly.controller.admin;



import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class DashboardController {
    

    @RequestMapping("/admin")
    public String getDashBoard() {
        return "admin/dashboard/show";
    }
}
