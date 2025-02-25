package com.example.quanly.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class OrderController {
    @RequestMapping("/admin/order")
    public String getDashBoard() {
        return "admin/order/show";
    }
}
