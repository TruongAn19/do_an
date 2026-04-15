package com.example.quanly.controller.admin;

import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
import com.example.quanly.service.RacketStockByDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/racket-stock")
@Slf4j
public class RacketStockByDateController {

    @Autowired
    private RacketStockByDateService racketStockService;

    @PostMapping("")
    public ResponseEntity<RacketStockByDate> checkStock(
            @RequestBody CheckStockRequest request) {
        log.info("--------+++++=");
        RacketStockByDate stock = racketStockService.getStock(request);
        return ResponseEntity.ok(stock);
    }
}