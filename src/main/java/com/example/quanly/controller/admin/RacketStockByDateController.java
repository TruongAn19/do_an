package com.example.quanly.controller.admin;

import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
import com.example.quanly.service.RacketService;
import com.example.quanly.service.RacketStockByDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/racket-stock")
public class RacketStockByDateController {

    @Autowired
    private RacketStockByDateService racketStockService;
    @PostMapping("")
    public ResponseEntity<RacketStockByDate> checkStock(
            @RequestBody CheckStockRequest request) {
        System.out.println("--------+++++=");
        RacketStockByDate stock = racketStockService.getStock(request);
        return ResponseEntity.ok(stock);
    }
}