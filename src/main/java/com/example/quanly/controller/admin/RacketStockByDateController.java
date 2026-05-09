package com.example.quanly.controller.admin;

import com.example.quanly.domain.RacketStockByDate;
import com.example.quanly.domain.dto.CheckStockRequest;
import com.example.quanly.service.RacketStockByDateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/racket-stock")
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RacketStockByDateController {

    RacketStockByDateService racketStockService;

    @PostMapping("")
    public ResponseEntity<RacketStockByDate> checkStock(@RequestBody CheckStockRequest request) {
        log.info("--------+++++=");
        RacketStockByDate stock = racketStockService.getStock(request);
        return ResponseEntity.ok(stock);
    }
}
